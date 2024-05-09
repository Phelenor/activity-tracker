package com.rafaelboban.activitytracker.wear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme

@Composable
fun StatisticItem(
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary,
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = createValueText(value, unit),
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun createValueText(value: String, unit: String? = null): AnnotatedString {
    return buildAnnotatedString {
        append(value)

        unit?.let {
            withStyle(SpanStyle(fontSize = 10.sp)) {
                append("$unit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticItemPreview() {
    ActivityTrackerWearTheme {
        StatisticItem(
            value = "3.2",
            unit = "km",
            icon = Icons.AutoMirrored.Default.TrendingUp
        )
    }
}
