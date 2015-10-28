package net.fybertech.blastproof;

import java.io.File;
import java.util.List;

import net.fybertech.meddle.Meddle;
import net.fybertech.meddle.MeddleMod;
import net.fybertech.meddleapi.ConfigFile;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;


@MeddleMod(id="blastproof", name="Blastproof", author="FyberOptic", version="1.0", depends={"dynamicmappings", "meddleapi"})
public class BlastTweak implements ITweaker
{
	public static boolean dropAllItems = true;
	public static boolean blastIgnoresItems = true;
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		File configFile = new File(Meddle.getConfigDir(), "blastproof.cfg");
		ConfigFile config = new ConfigFile(configFile);
		config.load();
		dropAllItems = config.get(config.key("general", "dropAllItems", true, "Explosions drop all affected blocks"));
		blastIgnoresItems = config.get(config.key("general", "blastIgnoresItems", true, "Prevent explosions from killing item entities"));
		if (config.hasChanged()) config.save();		
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		classLoader.registerTransformer(BlastTransformer.class.getName());
	}

	@Override
	public String getLaunchTarget() {
		return null;
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}

}
