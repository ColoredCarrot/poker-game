package render

import shared.RenderContext

interface StaticComponent {

    fun RenderContext.addToDOM()

}

fun StaticComponent.addToDOM(ctx: RenderContext) = with(ctx) { addToDOM() }
