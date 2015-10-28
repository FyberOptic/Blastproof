package net.fybertech.blastproof;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.Explosion;

public class BlastMod {

	public void init()
	{		
		// Do this to make it load at startup, to check for errors
		Explosion.class.getName();	
	}
	
	
	public static void modifyList(List<Entity> list) 
	{
		if (!BlastTweak.blastIgnoresItems) return;
		
		for (Iterator<Entity> it = list.iterator(); it.hasNext();) {
			Entity entity = it.next();
			if (entity instanceof EntityItem) it.remove();
		}
			
	}
	
	
}
