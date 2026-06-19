package es.upc.waypass.presentation.driver.company

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import es.upc.waypass.domain.model.CompanyMember
import es.upc.waypass.presentation.membership.MembershipViewModel
import es.upc.waypass.ui.theme.RedDestructive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyMembersScreen(
    companyId: Int,
    invitationCode: String?,
    onBackClick: () -> Unit,
    onCodeRegenerated: (String) -> Unit,
    viewModel: MembershipViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentCode by remember { mutableStateOf(invitationCode) }
    var showRegenerateDialog by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<CompanyMember?>(null) }

    LaunchedEffect(companyId) {
        viewModel.loadMembers(companyId)
    }

    LaunchedEffect(state.regeneratedCode) {
        state.regeneratedCode?.let { newCode ->
            currentCode = newCode
            onCodeRegenerated(newCode)
            snackbarHostState.showSnackbar("Código regenerado")
            viewModel.consumeAction()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeAction()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Miembros") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            InvitationCodeCard(
                code = currentCode,
                onCopy = {
                    currentCode?.let {
                        clipboardManager.setText(AnnotatedString(it))
                    }
                },
                onRegenerate = { showRegenerateDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Conductores",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.membersLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.members.size <= 1 -> {
                    Text(
                        text = "Aún no hay otros miembros. Comparte el código para invitar conductores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.members, key = { it.membershipId }) { member ->
                            MemberRow(
                                member = member,
                                onRemoveClick = { memberToRemove = member }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            title = { Text("Regenerar código") },
            text = { Text("Regenerar invalidará el código anterior. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    showRegenerateDialog = false
                    viewModel.regenerate(companyId)
                }) {
                    Text("Regenerar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text("Expulsar miembro") },
            text = { Text("¿Seguro que deseas expulsar a ${member.displayName}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.remove(member.membershipId, companyId)
                    memberToRemove = null
                }) {
                    Text("Expulsar", color = RedDestructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InvitationCodeCard(
    code: String?,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Código de invitación",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = code ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onCopy, enabled = code != null) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar código",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onRegenerate) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Regenerar código",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Compártelo con tus conductores para que se unan a la empresa.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun MemberRow(
    member: CompanyMember,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Se unió el ${formatJoinedAt(member.joinedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }

            RoleChip(isAdmin = member.isAdmin)

            if (!member.isAdmin) {
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Outlined.PersonRemove,
                        contentDescription = "Expulsar",
                        tint = RedDestructive
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleChip(isAdmin: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isAdmin) "Admin" else "Conductor",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatJoinedAt(raw: String): String {
    // El backend envía ISO-8601 (ej. "2026-06-18T10:30:00Z"); mostramos solo la fecha.
    val datePart = raw.substringBefore("T")
    return if (datePart.length == 10) datePart else raw
}
