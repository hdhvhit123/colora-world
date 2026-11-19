package com.colora.world.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.colora.world.graph
import com.colora.world.render.ArtModel
import com.colora.world.render.ArtRenderer
import com.colora.world.engine.paint.PaintState
import com.colora.world.ui.theme.C
import com.colora.world.ui.theme.D
import com.colora.world.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Press feedback: scale 0.97 over ~120 ms (spec §3.6). */
@Composable
fun Modifier.pressable(enabled: Boolean = true, role: Role = Role.Button, sound: Boolean = true, onClick: () -> Unit): Modifier {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val k by animateFloatAsState(if (pressed) 0.96f else 1f, tween(120), label = "press")
    val ctx = LocalContext.current
    return this.scale(k).clickable(src, null, enabled = enabled, role = role) { if (sound) ctx.graph.audio.sfx("ui.tap"); onClick() }
}

@Composable
fun BigButton(
    text: String, modifier: Modifier = Modifier, icon: Ic? = null, color: Color = C.coral, textColor: Color = C.ink,
    height: Dp = D.primary, enabled: Boolean = true, onClick: () -> Unit,
) {
    Row(
        modifier
            .defaultMinSize(minHeight = height)
            .pressable(enabled, onClick = onClick)
            .shadow(6.dp, RoundedCornerShape(D.buttonRadius), ambientColor = C.shadow, spotColor = C.shadow)
            .clip(RoundedCornerShape(D.buttonRadius))
            .background(if (enabled) color else C.line)
            .border(2.dp, C.ink.copy(alpha = 0.10f), RoundedCornerShape(D.buttonRadius))
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) { ColoraIcon(icon, size = 30.dp, accent = C.paper); Spacer(Modifier.width(10.dp)) }
        Text(text, style = T.heading.copy(color = textColor), maxLines = 2, textAlign = TextAlign.Center)
    }
}

@Composable
fun SoftButton(text: String, icon: Ic? = null, modifier: Modifier = Modifier, color: Color = C.paper, onClick: () -> Unit) {
    Row(
        modifier.defaultMinSize(minHeight = D.touch).pressable(onClick = onClick)
            .clip(RoundedCornerShape(20.dp)).background(color).border(2.dp, C.line, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { ColoraIcon(icon, size = 24.dp); Spacer(Modifier.width(8.dp)) }
        Text(text, style = T.label, maxLines = 2)
    }
}

@Composable
fun RoundIcon(ic: Ic, desc: String, modifier: Modifier = Modifier, bg: Color = C.paper, selected: Boolean = false, enabled: Boolean = true, size: Dp = D.touch, accent: Color = C.coral, onClick: () -> Unit) {
    Box(
        modifier.size(size)
            .semantics { contentDescription = desc; this.selected = selected }
            .pressable(enabled, onClick = onClick)
            .shadow(if (selected) 0.dp else 3.dp, CircleShape, ambientColor = C.shadow, spotColor = C.shadow)
            .clip(CircleShape)
            .background(if (selected) C.butter else bg)
            .border(if (selected) 3.dp else 2.dp, if (selected) C.focus else C.line, CircleShape),
        contentAlignment = Alignment.Center,
    ) { ColoraIcon(ic, size = size * 0.52f, tint = if (enabled) C.ink else C.ink.copy(alpha = 0.3f), accent = accent) }
}

@Composable
fun Panel(modifier: Modifier = Modifier, color: Color = C.paper, shape: Shape = RoundedCornerShape(D.cardRadius), padding: PaddingValues = PaddingValues(14.dp), content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier.shadow(5.dp, shape, ambientColor = C.shadow, spotColor = C.shadow).clip(shape).background(color)
            .border(BorderStroke(2.dp, C.ink.copy(alpha = 0.07f)), shape).padding(padding),
        content = content,
    )
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)?, backDesc: String, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = D.gutter, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) { RoundIcon(Ic.Back, backDesc, onClick = onBack); Spacer(Modifier.width(12.dp)) }
        Text(title, style = T.title, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        trailing()
    }
}

@Composable
fun BitmapImage(bmp: Bitmap?, desc: String?, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Fit) {
    if (bmp == null) { Box(modifier.background(C.sand.copy(alpha = 0.5f), RoundedCornerShape(16.dp))); return }
    val img = remember(bmp) { bmp.asImageBitmap() }
    Image(img, desc, modifier, contentScale = scale)
}

@Composable
fun AssetThumb(path: String, desc: String?, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Fit) {
    val ctx = LocalContext.current
    val bmp by produceState<Bitmap?>(ctx.graph.content.let { null }, path) { value = withContext(Dispatchers.IO) { ctx.graph.content.thumb(path) } }
    BitmapImage(bmp, desc, modifier, scale)
}

/** Live vector rendering of any artwork (used for small previews of saved work). */
@Composable
fun ArtView(art: ArtModel?, state: PaintState?, modifier: Modifier = Modifier, useSamples: Boolean = false, pad: Float = 0.06f) {
    val ctx = LocalContext.current
    Canvas(modifier) {
        val a = art ?: return@Canvas
        drawIntoCanvas { c -> ArtRenderer.drawFitted(c.nativeCanvas, a, state, size.width, size.height, pad, ArtRenderer.Options(useSamples = useSamples, stickerArt = ctx.graph.content::art)) }
    }
}

@Composable
fun Milo(expr: String, text: String?, modifier: Modifier = Modifier, size: Dp = 84.dp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AssetThumb("thumbs/milo.$expr.webp", null, Modifier.size(size))
        if (text != null) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)).background(C.paper).border(2.dp, C.line, RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(text, style = T.label)
            }
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, icon: Ic? = null, onClick: () -> Unit) {
    Row(
        modifier.defaultMinSize(minHeight = 48.dp).semantics { this.selected = selected }.pressable(onClick = onClick).clip(RoundedCornerShape(24.dp))
            .background(if (selected) C.butter else C.paper).border(if (selected) 3.dp else 2.dp, if (selected) C.focus else C.line, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) { ColoraIcon(Ic.Check, size = 18.dp); Spacer(Modifier.width(6.dp)) } else if (icon != null) { ColoraIcon(icon, size = 20.dp); Spacer(Modifier.width(6.dp)) }
        Text(text, style = T.small)
    }
}

@Composable
fun EmptyState(text: String, action: String?, expr: String = "curious", onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AssetThumb("thumbs/milo.$expr.webp", null, Modifier.size(120.dp))
        Spacer(Modifier.height(12.dp))
        Text(text, style = T.heading, textAlign = TextAlign.Center)
        if (action != null) { Spacer(Modifier.height(16.dp)); BigButton(action, icon = Ic.Plus, height = 60.dp, onClick = onAction) }
    }
}

@Composable
fun Toast(text: String?, onDone: () -> Unit) {
    if (text == null) return
    LaunchedEffect(text) { kotlinx.coroutines.delay(2600); onDone() }
    Box(Modifier.fillMaxSize().padding(bottom = 96.dp), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.shadow(8.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp)).background(C.ink).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(text, style = T.label.copy(color = C.paper))
        }
    }
}

fun labelStyle(color: Color): TextStyle = T.label.copy(color = color)
