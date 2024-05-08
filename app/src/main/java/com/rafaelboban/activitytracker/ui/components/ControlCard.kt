package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.ui.screens.main.dashboard.DashboardControl
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun ControlCard(
    control: DashboardControl,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(150.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                painter = painterResource(id = control.icon),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = control.titleRes),
                maxLines = 2,
                textAlign = TextAlign.Center,
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ControlCardPreview() {
    ActivityTrackerTheme {
        ControlCard(
            control = DashboardControl.INDIVIDUAL_ACTIVITY,
            onClick = {}
        )
    }
}
