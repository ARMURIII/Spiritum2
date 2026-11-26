package net.dark.spiritum.mixin.emi$;

import dev.emi.emi.jemi.impl.extras.JemiPlaceable;
import dev.emi.emi.jemi.impl.extras.JemiTextWidget;
import mezz.jei.api.gui.widgets.ITextWidget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(JemiTextWidget.class)
public abstract class JemiTextWidgetMixin extends JemiPlaceable<ITextWidget> {
    public JemiTextWidgetMixin(int width, int height) {super(width, height);}

    @Override
    public ITextWidget setPosition(int xPos, int yPos) {
        this.x = xPos;
        this.y = yPos;
        return (ITextWidget) this;
    }
}
