package redmaple.obscurerevival.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowEntity;

@Environment(EnvType.CLIENT)
public class ModifiedArrowEntityRenderer extends ProjectileEntityRenderer<ModifiedArrowEntity, ProjectileEntityRenderState> {

    public static final Identifier TEXTURE = Identifier.of("obscure_revival", "textures/entity/projectiles/modified_arrow.png");

    public ModifiedArrowEntityRenderer(EntityRendererFactory.Context context) { super(context); }

    // 创建渲染状态实例
    @Override
    public ProjectileEntityRenderState createRenderState() { return new ProjectileEntityRenderState(); }

    // 接收 ProjectileEntityRenderState 而不是 Entity
    @Override
    public Identifier getTexture(ProjectileEntityRenderState state) { return TEXTURE; }
}
