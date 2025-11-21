package com.example.marketelectronico.ui.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.marketelectronico.data.model.Notification
import com.example.marketelectronico.data.model.sampleNotifications
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
	navController: NavController,
	modifier: Modifier = Modifier,
	notifications: List<Notification> = sampleNotifications
) {
	val contentModifier = modifier
		.statusBarsPadding()
		.padding(top = 8.dp)

	Surface(modifier = contentModifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
		if (notifications.isEmpty()) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				Text("No tienes notificaciones", color = MaterialTheme.colorScheme.onBackground)
			}
		} else {
			LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				items(notifications) { n ->
					NotificationRow(notification = n, onClick = { /* navegar a detalle si se necesita */ })
				}
			}
		}
	}
}

@Composable
private fun NotificationRow(notification: Notification, onClick: () -> Unit) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(10.dp),
		colors = CardDefaults.cardColors(containerColor = if (notification.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
	) {
		Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
			Column(modifier = Modifier.weight(1f)) {
				Text(text = notification.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
				Spacer(modifier = Modifier.height(4.dp))
				Text(text = notification.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), maxLines = 2)
			}
			Column(horizontalAlignment = Alignment.End) {
				Text(text = notification.timestamp, fontSize = 12.sp, color = Color.Gray)
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
	MarketElectronicoTheme {
		NotificationsScreen(navController = rememberNavController())
	}
}

