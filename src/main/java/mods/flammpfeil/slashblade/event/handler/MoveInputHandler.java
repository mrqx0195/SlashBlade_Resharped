package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.client.SlashBladeKeyMappings;
import mods.flammpfeil.slashblade.event.client.MoveInputEvent;
import mods.flammpfeil.slashblade.network.MoveCommandMessage;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumSet;

@EventBusSubscriber(modid = SlashBlade.MODID, value = Dist.CLIENT)
public class MoveInputHandler {
    
    public static boolean checkFlag(int data, int flags) {
        return (data & flags) == flags;
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onPlayerPostTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        
        if (player.getMainHandItem().isEmpty() || BladeStateAccess.of(player.getMainHandItem()).isEmpty()) {
            return;
        }
        
        EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);
        
        if (player.input.up) {
            commands.add(InputCommand.FORWARD);
        }
        if (player.input.down) {
            commands.add(InputCommand.BACK);
        }
        if (player.input.left) {
            commands.add(InputCommand.LEFT);
        }
        if (player.input.right) {
            commands.add(InputCommand.RIGHT);
        }
        
        if (player.input.shiftKeyDown) {
            commands.add(InputCommand.SNEAK);
        }
        
        if (player.input.jumping) {
            commands.add(InputCommand.JUMP);
        }
        
        final Minecraft minecraftInstance = Minecraft.getInstance();
        
        if (SlashBladeKeyMappings.KEY_SPECIAL_MOVE.isDown()) {
            commands.add(InputCommand.SPRINT);
        }
        
        if (minecraftInstance.options.keyUse.isDown()) {
            commands.add(InputCommand.R_DOWN);
        }
        if (minecraftInstance.options.keyAttack.isDown()) {
            commands.add(InputCommand.L_DOWN);
        }
        
        if (SlashBladeKeyMappings.KEY_SUMMON_BLADE.isDown()) {
            commands.add(InputCommand.M_DOWN);
        }
        
        MoveInputEvent moveInputEvent = new MoveInputEvent(player, commands, event);
        NeoForge.EVENT_BUS.post(moveInputEvent);
        commands = moveInputEvent.getNewCommands();
        
        IInputState state = player.getData(CapabilityInputState.INPUT_STATE.get());
        EnumSet<InputCommand> old = state.getCommands().clone();
        long currentTime = player.level().getGameTime();
        boolean doSend = !old.equals(commands);
        
        if (doSend) {
            commands.forEach(c -> {
                if (!old.contains(c)) {
                    state.getLastPressTimes().put(c, currentTime);
                }
            });
            
            state.getCommands().clear();
            state.getCommands().addAll(commands);
            
            PacketDistributor.sendToServer(new MoveCommandMessage(commands));
        }
    }
}
