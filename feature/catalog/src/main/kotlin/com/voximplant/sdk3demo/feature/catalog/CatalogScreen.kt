import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.sdk3demo.core.designsystem.theme.Typography
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.feature.catalog.R
import com.voximplant.sdk3demo.feature.catalog.component.CatalogItem

@Composable
fun CatalogRoute(
    onModuleClick: (String) -> Unit,
) {
    CatalogScreen(onModuleClick)
}

@Composable
fun CatalogScreen(
    onModuleClick: (String) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.systemBarsPadding()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, false),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    CatalogItem(
                        title = "Audio call", description = "Make and receive an audio call", onClick = { onModuleClick("audio_call_route") }, image = painterResource(id = R.drawable.ic_phone_call_circle)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Version",
                    style = Typography.bodySmall,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewCatalogScreen() {
    VoximplantTheme {
        CatalogScreen(
            onModuleClick = {},
        )
    }
}
