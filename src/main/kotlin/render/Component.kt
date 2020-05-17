package render

import shared.RenderContext

abstract class Component : StaticComponent {

    override fun RenderContext.addToDOM() {
        addStaticToDOM()
//        update()
    }

    protected abstract fun RenderContext.addStaticToDOM()

    open fun update() {}

}
