package archidreamz.npcboundboxfix;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Mod(
   modid = "FixNpcBoundBox",
   name = "Npc碰撞箱修复",
   version = "1.1"
)
@SideOnly(Side.CLIENT)
public class NpcBoundBoxFix {
   private Class npcClass;
   private Method updateBoundBoxMethod;
   private int count;

   @EventHandler
   public void perLoad(FMLInitializationEvent evt) {
      MinecraftForge.EVENT_BUS.register(this);
      FMLCommonHandler.instance().bus().register(this);

      try {
         this.npcClass = Class.forName("noppes.npcs.entity.EntityCustomNpc");
         this.updateBoundBoxMethod = this.npcClass.getMethod("updateHitbox");
      } catch (Exception var3) {
         FMLLog.info("未找到CustomNpc !", new Object[0]);
         var3.printStackTrace();
      }

   }

   @SubscribeEvent
   public void onEntityJoinWorld(EntityJoinWorldEvent evt) {
      if (this.updateBoundBoxMethod != null && this.npcClass.isAssignableFrom(evt.entity.getClass())) {
         try {
            this.updateBoundBoxMethod.invoke(evt.entity);
            evt.entity.setPosition(evt.entity.posX, evt.entity.posY, evt.entity.posZ);
         } catch (Exception var3) {
         }
      }

   }

   @SubscribeEvent
   public void onClientTikc(ClientTickEvent evt) {
      if (evt.phase == Phase.END) {
         if (this.count++ % 20 != 0) {
            return;
         }

         World world = Minecraft.getMinecraft().theWorld;
         if (world != null) {
            Iterator var3 = world.loadedEntityList.iterator();

            while(true) {
               Object e;
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  e = var3.next();
               } while(!this.npcClass.isAssignableFrom(e.getClass()));

               Entity entity = (Entity)e;
               Chunk chunk = entity.worldObj.getChunkFromBlockCoords(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posZ));
               boolean find = false;
               List[] var8 = chunk.entityLists;
               int var9 = var8.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  List list = var8[var10];
                  Iterator var12 = list.iterator();

                  while(var12.hasNext()) {
                     Object obj = var12.next();
                     if (obj.equals(e)) {
                        find = true;
                        break;
                     }
                  }
               }

               if (!find) {
                  chunk.addEntity(entity);
               }
            }
         }
      }

   }
}
