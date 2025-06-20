package com.example.hashpay.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(
    onQRCodeScanned: (String) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var flashEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    var qrCodeText by remember { mutableStateOf("") }
    var hasScanned by remember { mutableStateOf(false) }

    // Animation for scanner line
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scannerLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scannerLine"
    )

    // Animation for scan success
    val scanSuccessAnim = remember { Animatable(0f) }
    LaunchedEffect(hasScanned) {
        if (hasScanned) {
            scanSuccessAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(300)
            )
        }
    }

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
    }

    LaunchedEffect(key1 = Unit) {
        permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionGranted) {
            // Camera Preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient()
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy, barcodeScanner) { scannedText ->
                                if (!hasScanned) {
                                    qrCodeText = scannedText
                                    hasScanned = true
                                    onQRCodeScanned(scannedText)
                                }
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("QRScanner", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Scanner Overlay
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scannerSize = size.width * 0.8f
                    val left = (size.width - scannerSize) / 2
                    val top = (size.height - scannerSize) / 2
                    val right = left + scannerSize
                    val bottom = top + scannerSize
                    val overlayColor = Color.Black.copy(alpha = 0.6f)

                    // Top rectangle
                    drawRect(
                        color = overlayColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, top)
                    )
                    // Bottom rectangle
                    drawRect(
                        color = overlayColor,
                        topLeft = Offset(0f, bottom),
                        size = Size(size.width, size.height - bottom)
                    )
                    // Left rectangle
                    drawRect(
                        color = overlayColor,
                        topLeft = Offset(0f, top),
                        size = Size(left, scannerSize)
                    )
                    // Right rectangle
                    drawRect(
                        color = overlayColor,
                        topLeft = Offset(right, top),
                        size = Size(size.width - right, scannerSize)
                    )

                    // Scanner corners
                    val cornerLength = scannerSize * 0.15f
                    val strokeWidth = 8f
                    val cornerColor = Color(0xFFB2FF59)

                    // Top-left corner
                    drawLine(
                        color = cornerColor,
                        start = Offset(left, top + cornerLength),
                        end = Offset(left, top),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = cornerColor,
                        start = Offset(left, top),
                        end = Offset(left + cornerLength, top),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Top-right corner
                    drawLine(
                        color = cornerColor,
                        start = Offset(left + scannerSize - cornerLength, top),
                        end = Offset(left + scannerSize, top),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = cornerColor,
                        start = Offset(left + scannerSize, top),
                        end = Offset(left + scannerSize, top + cornerLength),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Bottom-left corner
                    drawLine(
                        color = cornerColor,
                        start = Offset(left, top + scannerSize - cornerLength),
                        end = Offset(left, top + scannerSize),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = cornerColor,
                        start = Offset(left, top + scannerSize),
                        end = Offset(left + cornerLength, top + scannerSize),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Bottom-right corner
                    drawLine(
                        color = cornerColor,
                        start = Offset(left + scannerSize - cornerLength, top + scannerSize),
                        end = Offset(left + scannerSize, top + scannerSize),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = cornerColor,
                        start = Offset(left + scannerSize, top + scannerSize),
                        end = Offset(left + scannerSize, top + scannerSize - cornerLength),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Scanner line animation
                    if (!hasScanned) {
                        val lineY = top + (scannerSize * scannerLinePosition)
                        drawLine(
                            brush = SolidColor(Color(0xFFB2FF59)),
                            start = Offset(left + 10, lineY),
                            end = Offset(left + scannerSize - 10, lineY),
                            strokeWidth = 3f
                        )
                    }

                    // Success indicator
                    if (hasScanned && scanSuccessAnim.value > 0) {
                        // Draw a checkmark or success indicator
                        val path = Path().apply {
                            moveTo(left + scannerSize * 0.3f, top + scannerSize * 0.5f)
                            lineTo(left + scannerSize * 0.45f, top + scannerSize * 0.65f)
                            lineTo(left + scannerSize * 0.7f, top + scannerSize * 0.35f)
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFFB2FF59),
                            style = Stroke(width = 10f, cap = StrokeCap.Round),
                            alpha = scanSuccessAnim.value
                        )
                    }
                }

                // Instruction text
                Text(
                    text = if (hasScanned) "QR Code Detected!" else "Position QR code in the frame",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 60.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                // QR code result at the bottom
                AnimatedVisibility(
                    visible = hasScanned,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scanned Result:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = qrCodeText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFB2FF59)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onBackPressed() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB2FF59),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Continue")
                            }
                        }
                    }
                }

                // Control buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(24.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Flashlight button
                    IconButton(
                        onClick = {
                            flashEnabled = !flashEnabled
                            camera?.cameraControl?.enableTorch(flashEnabled)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(24.dp))
                    ) {
                    }
                }
            }
        } else {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "We need camera access to scan QR codes",
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB2FF59),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                barcodes.firstOrNull()?.rawValue?.let { scannedText ->
                    onResult(scannedText)
                }
            }
        }
        .addOnFailureListener { exception ->
            Log.e("QRScanner", "QR Code scanning failed", exception)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
