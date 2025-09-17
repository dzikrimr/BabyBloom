package com.example.bubtrack.presentation.ai.growthanalysis.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.BubTrackTheme

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val senderName: String = if (isFromUser) "Anda" else "Gemini AI"
)

@Composable
fun AiChatSection(
    modifier: Modifier = Modifier,
    chatMessages: List<ChatMessage> = emptyList(),
    isLoading: Boolean = false,
    onSendMessage: (String) -> Unit
) {
    var chatMessage by remember { mutableStateOf("") }

    val defaultMessages = if (chatMessages.isEmpty()) {
        listOf(
            ChatMessage(
                text = "Halo! Saya di sini untuk membantu Anda memahami pola tumbuh kembang bayi. Silakan tanyakan apa saja!",
                isFromUser = false
            )
        )
    } else {
        chatMessages
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF8B5CF6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_robot),
                        contentDescription = "AI Robot",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "Gemini AI Assistant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat messages
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                defaultMessages.forEach { message ->
                    ChatMessageItem(message = message)
                }
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatMessage,
                    onValueChange = { chatMessage = it },
                    placeholder = {
                        Text(
                            "Tanyakan tentang tumbuh kembang bayi Anda...",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    enabled = !isLoading
                )

                FloatingActionButton(
                    onClick = {
                        if (chatMessage.isNotBlank() && !isLoading) {
                            onSendMessage(chatMessage)
                            chatMessage = ""
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = Color(0xFF8B5CF6),
                    contentColor = Color.White
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_send),
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    if (message.isFromUser) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFA855F7)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFF9CA3AF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiChatSectionPreview() {
    BubTrackTheme {
        AiChatSection(
            modifier = Modifier.padding(16.dp),
            onSendMessage = {}
        )
    }
}