package eu.kanade.presentation.browse.components

import android.content.pm.PackageManager
import android.util.DisplayMetrics
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import eu.kanade.domain.animesource.model.AnimeSource
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.extension.model.AnimeExtension
import eu.kanade.tachiyomi.util.lang.withIOContext

private val defaultModifier = Modifier
    .height(40.dp)
    .aspectRatio(1f)

@Composable
fun AnimeSourceIcon(
    source: AnimeSource,
    modifier: Modifier = Modifier,
) {
    val icon = source.icon

    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = "",
            modifier = modifier.then(defaultModifier),
        )
    } else {
        Image(
            painter = painterResource(id = R.mipmap.ic_local_source),
            contentDescription = "",
            modifier = modifier.then(defaultModifier),
        )
    }
}

@Composable
fun ExtensionIcon(
    extension: AnimeExtension,
    modifier: Modifier = Modifier,
    density: Int = DisplayMetrics.DENSITY_DEFAULT,
) {
    when (extension) {
        is AnimeExtension.Available -> {
            AsyncImage(
                model = extension.iconUrl,
                contentDescription = "",
                placeholder = ColorPainter(Color(0x1F888888)),
                error = rememberResourceBitmapPainter(id = R.drawable.cover_error),
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .then(defaultModifier),
            )
        }
        is AnimeExtension.Installed -> {
            val icon by extension.getIcon(density)
            when (icon) {
                Result.Error -> Image(
                    bitmap = ImageBitmap.imageResource(id = R.mipmap.ic_local_source),
                    contentDescription = "",
                    modifier = modifier.then(defaultModifier),
                )
                Result.Loading -> Box(modifier = modifier.then(defaultModifier))
                is Result.Success -> Image(
                    bitmap = (icon as Result.Success<ImageBitmap>).value,
                    contentDescription = "",
                    modifier = modifier.then(defaultModifier),
                )
            }
        }
        is AnimeExtension.Untrusted -> Image(
            bitmap = ImageBitmap.imageResource(id = R.mipmap.ic_untrusted_source),
            contentDescription = "",
            modifier = modifier.then(defaultModifier),
        )
    }
}

@Composable
private fun AnimeExtension.getIcon(density: Int = DisplayMetrics.DENSITY_DEFAULT): State<Result<ImageBitmap>> {
    val context = LocalContext.current
    return produceState<Result<ImageBitmap>>(initialValue = Result.Loading, this) {
        withIOContext {
            value = try {
                val appInfo = context.packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
                val appResources = context.packageManager.getResourcesForApplication(appInfo)
                Result.Success(
                    appResources.getDrawableForDensity(appInfo.icon, density, null)!!
                        .toBitmap()
                        .asImageBitmap(),
                )
            } catch (e: Exception) {
                Result.Error
            }
        }
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    object Error : Result<Nothing>()
    data class Success<out T>(val value: T) : Result<T>()
}
