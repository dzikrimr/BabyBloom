package com.example.bubtrack.presentation.ai.sleepmonitor.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bubtrack.R

@Composable
fun DevicePairingPopup(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Show QR Code") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Device Pairing",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color(0xFFF3F4F6), CircleShape)
                            .size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFFF3F0F8))
                        .padding(4.dp)
                ) {
                    TabItem(
                        text = "Show QR Code",
                        selected = selectedTab == "Show QR Code",
                        modifier = Modifier.weight(1f),
                        icon = painterResource(id = R.drawable.ic_qr_scan),
                        onClick = { selectedTab = "Show QR Code" }
                    )
                    TabItem(
                        text = "Scan QR Code",
                        selected = selectedTab == "Scan QR Code",
                        modifier = Modifier.weight(1f),
                        icon = painterResource(id = R.drawable.ic_camera),
                        onClick = { selectedTab = "Scan QR Code" }
                    )
                }
                Spacer(Modifier.height(20.dp))
                if (selectedTab == "Show QR Code") {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_qr_scan),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(160.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Scan to connect and monitor baby remotely",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4B5563),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    InstructionItem(1, "Open BabyBloom app on the other device")
                    InstructionItem(2, "Tap \"Pair Device\" and scan this QR code")
                    InstructionItem(3, "View live feed and receive notifications")
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { /* Share logic */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B8AFB)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Share QR Code", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { /* Save logic */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF9F9F9F)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_save),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF374151)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Save", color = Color(0xFF374151), fontSize = 12.sp)
                        }
                        Button(
                            onClick = { /* Copy logic */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF9F9F9F)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF374151)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Link", color = Color(0xFF374151), fontSize = 11.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(500.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Camera Scanner Placeholder",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Point your camera at the QR code on the other device for connect",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4B5563),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFF9B8AFB) else Color.Transparent
    val textColor = if (selected) Color.White else Color.Black.copy(alpha = 0.6f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InstructionItem(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF93C5FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Preview(showBackground = true)
@Composable
fun DevicePairingPopupPreview() {
    MaterialTheme {
        DevicePairingPopup(onDismiss = {})
    }
}