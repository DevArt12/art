
package com.example.artgallery.ui.ar

import android.content.Context
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import java.lang.ref.WeakReference

class ARModelRenderer(context: Context) {
    private val contextRef = WeakReference(context)
    private var currentAnchorNode: AnchorNode? = null
    
    fun loadModel(
        fragment: ArFragment,
        anchor: Anchor,
        modelResId: Int,
        onModelPlaced: () -> Unit
    ) {
        contextRef.get()?.let { context ->
            ModelRenderable.builder()
                .setSource(context, modelResId)
                .build()
                .thenAccept { renderable ->
                    addNodeToScene(fragment, anchor, renderable)
                    onModelPlaced()
                }
                .exceptionally { throwable ->
                    throw AssertionError("Error loading model", throwable)
                }
        }
    }

    private fun addNodeToScene(
        fragment: ArFragment,
        anchor: Anchor,
        renderable: ModelRenderable
    ) {
        val anchorNode = AnchorNode(anchor).apply {
            setParent(fragment.arSceneView.scene)
        }
        anchorNode.renderable = renderable
        currentAnchorNode = anchorNode
    }

    fun clear() {
        currentAnchorNode?.let { node ->
            node.anchor?.detach()
            node.setParent(null)
        }
        currentAnchorNode = null
    }
}
