package app.confused.anikuta.feature.backup.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.confused.anikuta.core.backup.AutoBackupFrequency
import app.confused.anikuta.core.designsystem.theme.RobotoFamily

/**
 * A 4-option segmented control for auto-backup frequency selection.
 *
 * The existing [TwoWayToggle]/[ThreeWayToggle] only support 2/3 options, so
 * this custom control handles 4. Active segment is filled with primary color;
 * inactive are surfaceVariant. Matches the design language (principle #8).
 *
 * @param selected the currently selected frequency.
 * @param onSelect called when a frequency is tapped.
 */
@Composable
fun FrequencySelector(
    selected: AutoBackupFrequency,
    onSelect: (AutoBackupFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AutoBackupFrequency.entries.forEach { freq ->
            val isSelected = freq == selected
            Surface(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(freq) },
            ) {
                Text(
                    text = freq.displayName,
                    fontFamily = RobotoFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                )
            }
        }
    }
}
