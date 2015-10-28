package net.fybertech.blastproof;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.sun.javafx.geom.transform.GeneralTransform3D;

import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.meddle.Meddle;
import net.minecraft.launchwrapper.IClassTransformer;

public class BlastTransformer implements IClassTransformer
{

	String explosion_name = DynamicMappings.getClassMapping("net/minecraft/world/Explosion");
	
	@Override
	public byte[] transform(String name, String transformerName, byte[] bytes) 
	{
		if (explosion_name != null && name.equals(explosion_name)) return transformExplosion(bytes);
		else return bytes;
	}

	
	private byte[] failGracefully(String error, byte[] bytes)
	{
		Meddle.LOGGER.error("[Meddle/Blastproof] " + error);
		return bytes;
	}

	
	public byte[] transformExplosion(byte[] bytes)
	{
		ClassReader reader = new ClassReader(bytes);
		ClassNode cn = new ClassNode();
		reader.accept(cn, 0);
		
		
		String getEntities = DynamicMappings.getMethodMapping("net/minecraft/world/World getEntitiesWithinAABBExcludingEntity (Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;");
		MethodNode explosionA = DynamicMappings.getMethodNodeFromMapping(cn, "net/minecraft/world/Explosion doExplosionA ()V");
		if (explosionA != null && getEntities != null) 
		{			
			for (AbstractInsnNode insn = explosionA.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				AbstractInsnNode[] nodes = DynamicMappings.getOpcodeSequenceArray(insn, Opcodes.INVOKEVIRTUAL, Opcodes.ASTORE, Opcodes.NEW);
				if (nodes == null) continue;
				MethodInsnNode mn = (MethodInsnNode)nodes[0];
				if (!getEntities.equals(mn.owner + " " + mn.name + " " + mn.desc)) continue;
				VarInsnNode vn = (VarInsnNode)nodes[1];
				
				InsnList list = new InsnList();
				list.add(new VarInsnNode(Opcodes.ALOAD, vn.var));
				list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/fybertech/blastproof/BlastMod", "modifyList", "(Ljava/util/List;)V", false));
				
				Meddle.LOGGER.info("[Meddle/Blastproof] Patching Explosion.doExplosionA");
				explosionA.instructions.insertBefore(nodes[2], list);
				break;
			}
		}
		else Meddle.LOGGER.info("[Meddle/Blastproof] Unable to locate mappings to patch Explosion.doExplosionA!");
		
		
		
		//"net/minecraft/block/Block dropBlockAsItemWithChance (Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;FI)V",
		//"net/minecraft/block/Block dropBlockAsItem (Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;I)V"
		MethodNode explosionB = DynamicMappings.getMethodNodeFromMapping(cn, "net/minecraft/world/Explosion doExplosionB (Z)V");
		if (explosionB == null) return failGracefully("Unable to locate doExplosionB!", bytes);
		
		// FCONST_1
	    // ALOAD 0
	    // GETFIELD net/minecraft/Explosion.i : F
	    // FDIV
		
		if (BlastTweak.dropAllItems) {
			AbstractInsnNode[] nodes = null;
			for (AbstractInsnNode insn = explosionB.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				nodes = DynamicMappings.getOpcodeSequenceArray(insn, Opcodes.FCONST_1, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.FDIV);
				if (nodes != null) break;						
			}
			
			if (nodes != null) {
				Meddle.LOGGER.info("[Meddle/Blastproof] Patching Explosion.doExplosionB");
				for (int n = 1; n < nodes.length; n++) {
					explosionB.instructions.remove(nodes[n]);
				}
			}
			else return failGracefully("Unable to patch doExplosionB!", bytes);		
		}
		else Meddle.LOGGER.info("[Meddle/Blastproof] dropAllItems is false, not patching Explosion.doExplosionB");
		
		
		ClassWriter writer = new ClassWriter(0);
		cn.accept(writer);
		return writer.toByteArray();
	}
	
	
}
