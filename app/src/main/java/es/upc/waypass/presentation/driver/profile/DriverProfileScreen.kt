package es.upc.waypass.presentation.driver.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import es.upc.waypass.presentation.membership.MembershipViewModel
import es.upc.waypass.ui.theme.RedDestructive

@Composable
fun DriverProfileScreen(
    userId: Int,
    companyName: String,
    memberRole: String,                 // "Admin" | "Driver"
    invitationCode: String?,            // no-null solo si admin
    hasActiveSubscription: Boolean,
    onSubscribeClick: () -> Unit,
    onManageMembersClick: () -> Unit,   // solo admin
    onLeaveCompanyClick: () -> Unit,    // solo driver
    onLogoutClick: () -> Unit,
    viewModel: MembershipViewModel = hiltViewModel()  // para "salir"
) {
    val isAdmin = memberRole == "Admin"
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            viewModel.consumeAction()
            onLeaveCompanyClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Información general de tu cuenta de conductor",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = companyName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isAdmin) "Administrador" else "Conductor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ProfileInfoCard(
            icon = Icons.Default.Badge,
            title = "ID de usuario",
            value = userId.toString()
        )

        ProfileInfoCard(
            icon = Icons.Default.Business,
            title = "Empresa asociada",
            value = companyName
        )

        ProfileInfoCard(
            icon = Icons.Outlined.Badge,
            title = "Rol",
            value = if (isAdmin) "Administrador" else "Conductor"
        )

        ProfileInfoCard(
            icon = Icons.Default.Verified,
            title = "Estado",
            value = if (hasActiveSubscription) "Activo" else "Inactivo",
            valueColor = if (hasActiveSubscription)
                Color(0xFF16A34A)
            else
                Color(0xFF6B7280)
        )

        // ─── Bloque específico de Admin ──────────────────────────────────────
        if (isAdmin) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Código de invitación",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = invitationCode ?: "—",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                invitationCode?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(
                                        context,
                                        "Código copiado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = invitationCode != null
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copiar código",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onManageMembersClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gestionar miembros")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Suscripción de la empresa ───────────────────────────────────────
        // Solo el Admin paga: únicamente él ve y opera el botón de suscripción.
        if (isAdmin) {
            Button(
                onClick = onSubscribeClick,
                enabled = !hasActiveSubscription,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF9CA3AF)
                )
            ) {

                Text(
                    text = if (hasActiveSubscription)
                        "Premium Activo"
                    else
                        "Suscribirse a Premium"
                )
            }
        }

        // ─── Salir de la empresa (solo Driver) ───────────────────────────────
        if (!isAdmin) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showLeaveDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedDestructive
                )
            ) {
                Text("Salir de la empresa")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC2626)
            )
        ) {

            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar sesión"
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Cerrar sesión")

        }

        Spacer(
            modifier = Modifier
                .height(90.dp)
                .navigationBarsPadding()
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Salir de la empresa") },
            text = { Text("¿Seguro que deseas salir de $companyName? Perderás el acceso a sus rutas y paraderos.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.leave()
                }) {
                    Text("Salir", color = RedDestructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = valueColor
                )
            }
        }
    }
}
