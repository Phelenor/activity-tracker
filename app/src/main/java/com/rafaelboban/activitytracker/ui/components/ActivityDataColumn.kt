package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun ActivityDataColumn(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: ImageVector? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.labelLarge,
                color = contentColor
            )
            icon?.let {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = icon,
                    tint = contentColor,
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = createValueText(value, unit),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Typography.displayLarge,
            fontSize = 20.sp,
            color = contentColor
        )
    }
}

private fun createValueText(value: String, unit: String? = null): AnnotatedString {
    return buildAnnotatedString {
        append(value)

        unit?.let {
            withStyle(SpanStyle(fontSize = 13.sp)) {
                append(" $unit")
            }
        }
    }
}

@Preview
@Composable
private fun ActivityDataColumnPreview() {
    ActivityTrackerTheme {
        ActivityDataColumn(
            title = "Distance",
            value = "3.52",
            unit = "km"
        )
    }
}

@Preview
@Composable
private fun ActivityDataColumnWithIconPreview() {
    ActivityTrackerTheme {
        ActivityDataColumn(
            title = "Duration",
            value = "01:02:52",
            icon = Icons.Filled.Timer
        )
    }
}
