package me.sxmurai.inferno.impl.features.module.modules.miscellaneous;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Declaration(name = "TickShift", category = Category.Exploits)
public class TickShift extends Module {
	 	
    BooleanSetting disable = registerBoolean("Disable", true);	
    IntegerSetting disableTicks = registerInteger("DisableTicks", 26, 1, 100);
    BooleanSetting movementEnable = registerBoolean("MovementEnable", true);		
    IntegerSetting enableTicks = registerInteger("EnableTicks", 30, 1, 100);
    DoubleSetting multiplier = registerDouble("Multiplier", 3.0, 1.0, 10.0);
	
    private int ticksPassed = 0;
    private int ticksStill = 0;
    private boolean playerMoving;
    private boolean timerOn = false;
	
    public void onUpdate() {
	 if(timerOn == false) {
		if(isMoving(mc.player)) {
			if(ticksStill >= 1) {
				ticksStill--;	
			}
			
	 	} else if (!isMoving(mc.player)) {
	      		ticksStill++;	 
	 	} 
	 }
	 
	 
	 if(ticksStill >= enableTicks.getValue()) {
		 timerOn = true;
		 if(movementEnable.getValue()) { 
			if(mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()) { 
				mc.timer.tickLength = ((float) (50.0 / multiplier.getValue()));  
				ticksPassed++; 
		 	}	  
		 } else {
			mc.timer.tickLength = ((float) (50.0 / multiplier.getValue())); 
			ticksPassed++; 		 
		 }
		 
		 
	 }
	    
	if(ticksPassed >= disableTicks.getValue()) {
	    	ticksPassed = 0;
		if(disable.getValue()) {
			disable();
		} else {
			reset();	
		}
		
	 }
    }
	
    public static boolean isMoving(EntityLivingBase entity) {
        return entity.moveForward != 0 || entity.moveStrafing != 0;
    }		
	  
    public void onDisable() {
	timerOn = false;
	ticksStill = 0;
    	mc.timer.tickLength = 50f;    
    }
		
    public void reset() {
	timerOn = false;
	ticksStill = 0;
    	mc.timer.tickLength = 50f;   
    }
 	
    public String getHudInfo() {
	return "[" + ChatFormatting.WHITE + String.valueOf(ticksStill) + ChatFormatting.GRAY + "]"; 
    }
 

}
