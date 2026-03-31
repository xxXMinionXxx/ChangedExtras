package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiBufferSource {
   static MultiBufferSource.BufferSource m_109898_(BufferBuilder p_109899_) {
      return m_109900_(ImmutableMap.of(), p_109899_);
   }

   static MultiBufferSource.BufferSource m_109900_(Map<RenderType, BufferBuilder> p_109901_, BufferBuilder p_109902_) {
      return new MultiBufferSource.BufferSource(p_109902_, p_109901_);
   }

   VertexConsumer m_6299_(RenderType p_109903_);

   @OnlyIn(Dist.CLIENT)
   public static class BufferSource implements MultiBufferSource {
      protected final BufferBuilder f_109904_;
      protected final Map<RenderType, BufferBuilder> f_109905_;
      protected Optional<RenderType> f_109906_ = Optional.empty();
      protected final Set<BufferBuilder> f_109907_ = Sets.newHashSet();

      protected BufferSource(BufferBuilder p_109909_, Map<RenderType, BufferBuilder> p_109910_) {
         this.f_109904_ = p_109909_;
         this.f_109905_ = p_109910_;
      }

      public VertexConsumer m_6299_(RenderType p_109919_) {
         Optional<RenderType> optional = p_109919_.m_110406_();
         BufferBuilder bufferbuilder = this.m_109914_(p_109919_);
         if (!Objects.equals(this.f_109906_, optional) || !p_109919_.m_234326_()) {
            if (this.f_109906_.isPresent()) {
               RenderType rendertype = this.f_109906_.get();
               if (!this.f_109905_.containsKey(rendertype)) {
                  this.m_109912_(rendertype);
               }
            }

            if (this.f_109907_.add(bufferbuilder)) {
               bufferbuilder.m_166779_(p_109919_.m_173186_(), p_109919_.m_110508_());
            }

            this.f_109906_ = optional;
         }

         return bufferbuilder;
      }

      private BufferBuilder m_109914_(RenderType p_109915_) {
         return this.f_109905_.getOrDefault(p_109915_, this.f_109904_);
      }

      public void m_173043_() {
         if (this.f_109906_.isPresent()) {
            RenderType rendertype = this.f_109906_.get();
            if (!this.f_109905_.containsKey(rendertype)) {
               this.m_109912_(rendertype);
            }

            this.f_109906_ = Optional.empty();
         }

      }

      public void m_109911_() {
         this.f_109906_.ifPresent((p_109917_) -> {
            VertexConsumer vertexconsumer = this.m_6299_(p_109917_);
            if (vertexconsumer == this.f_109904_) {
               this.m_109912_(p_109917_);
            }

         });

         for(RenderType rendertype : this.f_109905_.keySet()) {
            this.m_109912_(rendertype);
         }

      }

      public void m_109912_(RenderType p_109913_) {
         BufferBuilder bufferbuilder = this.m_109914_(p_109913_);
         boolean flag = Objects.equals(this.f_109906_, p_109913_.m_110406_());
         if (flag || bufferbuilder != this.f_109904_) {
            if (this.f_109907_.remove(bufferbuilder)) {
               p_109913_.m_276775_(bufferbuilder, RenderSystem.getVertexSorting());
               if (flag) {
                  this.f_109906_ = Optional.empty();
               }

            }
         }
      }
   }
}