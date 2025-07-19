package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.udea.compumovil.gr07_20251.udeafood.R

@Composable
fun UserTypeSelectionScreen(
    onClientSelected: () -> Unit,
    onStoreSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7FD)) // Fondo claro
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo UdeaFood centrado
        Image(
            painter = painterResource(id = R.drawable.welcome_icon),
            contentDescription = "Logo de UdeaFood",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Bienvenido a UdeaFood",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Qué tipo de cuenta deseas usar?",
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onStoreSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F508C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Soy una Tienda ", color = Color.White, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClientSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F508C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Soy un Cliente ", color = Color.White, fontSize = 15.sp)
        }
    }
}