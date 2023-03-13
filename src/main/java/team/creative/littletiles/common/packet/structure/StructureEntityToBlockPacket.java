package team.creative.littletiles.common.packet.structure;

import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public class StructureEntityToBlockPacket extends StructurePacket {
    
    public StructureEntityToBlockPacket() {}
    
    public StructureEntityToBlockPacket(StructureLocation location) {
        super(location);
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        // TODO add take rendering stuff
    }
    
}
