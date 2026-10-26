package com.example.financetracker.feature.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.navigation.AddExpensePrefill
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.CameraColors
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

// Route: scan. Po přečtení kódu zavolat onScanned(prefill) → otevře se předvyplněný formulář výdaje.
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onScanned: (AddExpensePrefill) -> Unit,
) {
    val context = LocalContext.current
    val hasCamera = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
    var hasPermission by remember { mutableStateOf(context.isCameraPermissionGranted()) }
    var permissionAsked by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        permissionAsked = true
    }
    LaunchedEffect(Unit) {
        if (hasCamera && !hasPermission && !permissionAsked) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var status by remember { mutableStateOf<ScanStatus>(ScanStatus.Aiming) }
    var torchOn by remember { mutableStateOf(false) }
    var hasTorch by remember { mutableStateOf(false) }

    val scanner = remember { BarcodeScanning.getClient(ScannerOptions) }
    DisposableEffect(scanner) { onDispose { scanner.close() } }

    // Krátce ukážeme „Načítám kód…“, ať uživatel vidí, že se kód chytil, a pak otevřeme formulář
    val currentOnScanned by rememberUpdatedState(onScanned)
    LaunchedEffect(status) {
        val reading = status as? ScanStatus.Reading ?: return@LaunchedEffect
        delay(ReadingFeedbackMillis)
        currentOnScanned(reading.prefill)
    }

    val onBarcodes: (List<Barcode>) -> Unit = { barcodes ->
        if (status == ScanStatus.Aiming) status = barcodes.toScanStatus()
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scanImage(context, scanner, uri) { status = it }
    }
    val pickFromGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LightSystemBarIcons()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraColors.Background),
    ) {
        val cameraReady = hasCamera && hasPermission
        if (cameraReady) {
            CameraPreview(
                scanner = scanner,
                analyzing = status == ScanStatus.Aiming,
                torchOn = torchOn,
                onTorchAvailable = { hasTorch = it },
                onBarcodes = onBarcodes,
                modifier = Modifier.fillMaxSize(),
            )
            ViewfinderOverlay(
                state = when (status) {
                    ScanStatus.Aiming -> ViewfinderState.Aiming
                    is ScanStatus.Reading -> ViewfinderState.Detected
                    is ScanStatus.Failed -> ViewfinderState.Error
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        CameraTopBar(onBack = onBack, modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val failed = status as? ScanStatus.Failed
            when {
                failed != null -> ScanErrorCard(
                    title = failed.error.title,
                    message = failed.error.message,
                    primaryText = "Skenovat znovu",
                    onPrimary = { status = ScanStatus.Aiming },
                    secondaryText = "Zadat ručně",
                    onSecondary = { onScanned(AddExpensePrefill()) },
                )

                !hasCamera -> ScanErrorCard(
                    title = "Zařízení nemá kameru",
                    message = "Vyberte fotku QR kódu nebo čárového kódu z galerie.",
                    primaryText = "Vybrat z galerie",
                    onPrimary = pickFromGallery,
                    icon = Icons.Outlined.NoPhotography,
                )

                !hasPermission -> {
                    // Po trvalém odmítnutí už systém dialog neukáže → odkaz do nastavení aplikace
                    val activity = context as? Activity
                    val canAskAgain = !permissionAsked || (activity != null &&
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA))
                    ScanErrorCard(
                        title = "Bez přístupu ke kameře",
                        message = "Kameru potřebujeme ke čtení QR kódů. Kód můžete také vybrat z fotky v galerii.",
                        primaryText = if (canAskAgain) "Povolit kameru" else "Otevřít nastavení",
                        onPrimary = {
                            if (canAskAgain) permissionLauncher.launch(Manifest.permission.CAMERA)
                            else context.openAppSettings()
                        },
                        secondaryText = "Z galerie",
                        onSecondary = pickFromGallery,
                        icon = Icons.Outlined.NoPhotography,
                    )
                }

                else -> {
                    ScanStatusPill(reading = status is ScanStatus.Reading)
                    Spacer(Modifier.height(Spacing.xxl))
                    CameraControls(
                        torchOn = torchOn,
                        hasTorch = hasTorch,
                        onTorchChange = { torchOn = it },
                        onGallery = pickFromGallery,
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- Stav skenu */

private sealed interface ScanStatus {
    data object Aiming : ScanStatus
    data class Reading(val prefill: AddExpensePrefill) : ScanStatus
    data class Failed(val error: ScanError) : ScanStatus
}

private enum class ScanError(val title: String, val message: String) {
    NotPayment(
        title = "Tohle není QR Platba",
        message = "Kód jsme přečetli, ale nejsou v něm platební údaje. Zkuste jiný kód, nebo výdaj zadejte ručně.",
    ),
    NoCodeInImage(
        title = "Na obrázku není kód",
        message = "Vyberte ostřejší fotku, na které je celý QR kód nebo čárový kód.",
    ),
    ImageUnreadable(
        title = "Obrázek se nepodařilo načíst",
        message = "Zkuste vybrat jinou fotku.",
    ),
}

/** První kód, který jde převést na výdaj; jinak chyba „není QR Platba“. */
private fun List<Barcode>.toScanStatus(): ScanStatus {
    if (isEmpty()) return ScanStatus.Failed(ScanError.NoCodeInImage)
    val prefill = firstNotNullOfOrNull { barcode ->
        val raw = barcode.rawValue ?: return@firstNotNullOfOrNull null
        prefillFromScannedCode(raw, barcode.codeType)
    }
    return if (prefill != null) ScanStatus.Reading(prefill) else ScanStatus.Failed(ScanError.NotPayment)
}

private val Barcode.codeType: ScannedCodeType
    get() = if (format == Barcode.FORMAT_QR_CODE) ScannedCodeType.QR else ScannedCodeType.PRODUCT_BARCODE

/** QR Platba + čárové kódy zboží. */
private val ScannerOptions = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(
        Barcode.FORMAT_QR_CODE,
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E,
    )
    .build()

private const val ReadingFeedbackMillis = 600L

/** Kód z fotky v galerii. */
private fun scanImage(
    context: android.content.Context,
    scanner: BarcodeScanner,
    uri: Uri,
    onResult: (ScanStatus) -> Unit,
) {
    val image = runCatching { InputImage.fromFilePath(context, uri) }.getOrElse {
        onResult(ScanStatus.Failed(ScanError.ImageUnreadable))
        return
    }
    scanner.process(image)
        .addOnSuccessListener { barcodes -> onResult(barcodes.toScanStatus()) }
        .addOnFailureListener { onResult(ScanStatus.Failed(ScanError.ImageUnreadable)) }
}

/* ---------------------------------------------------------------- Kamera */

/**
 * Náhled kamery (CameraX) s průběžným čtením kódů přes ML Kit.
 * Snímky se analyzují jen při [analyzing], jinak se rovnou zahodí.
 */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    scanner: BarcodeScanner,
    analyzing: Boolean,
    torchOn: Boolean,
    onTorchAvailable: (Boolean) -> Unit,
    onBarcodes: (List<Barcode>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }
    val currentAnalyzing by rememberUpdatedState(analyzing)
    val currentOnBarcodes by rememberUpdatedState(onBarcodes)
    val currentOnTorchAvailable by rememberUpdatedState(onTorchAvailable)

    DisposableEffect(controller, lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        controller.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        controller.setImageAnalysisAnalyzer(executor) { proxy ->
            val mediaImage = proxy.image
            if (mediaImage == null || !currentAnalyzing) {
                proxy.close()
                return@setImageAnalysisAnalyzer
            }
            scanner.process(InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { barcodes -> if (barcodes.isNotEmpty() && currentAnalyzing) currentOnBarcodes(barcodes) }
                .addOnCompleteListener { proxy.close() }
        }
        controller.bindToLifecycle(lifecycleOwner)
        controller.initializationFuture.addListener(
            { currentOnTorchAvailable(controller.cameraInfo?.hasFlashUnit() == true) },
            ContextCompat.getMainExecutor(context),
        )
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            executor.shutdown()
        }
    }

    LaunchedEffect(controller, torchOn) { controller.enableTorch(torchOn) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                // TextureView – náhled se pak správně skládá s Compose překryvem
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                this.controller = controller
            }
        },
    )
}

private enum class ViewfinderState { Aiming, Detected, Error }

/** Ztmavení kolem čtverce hledáčku a jeho rohy; při přečtení kódu se hledáček zvýrazní. */
@Composable
private fun ViewfinderOverlay(state: ViewfinderState, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val side = Dimens.ViewfinderSize.toPx()
        val topLeft = Offset((size.width - side) / 2, (size.height - side) / 2)
        val frame = Path().apply {
            addOutline(AppShapes.Viewfinder.createOutline(Size(side, side), layoutDirection, this@Canvas))
            translate(topLeft)
        }

        clipPath(frame, ClipOp.Difference) { drawRect(CameraColors.Scrim) }

        val cornerColor = when (state) {
            ViewfinderState.Aiming -> CameraColors.Content
            ViewfinderState.Detected -> CameraColors.Detected
            ViewfinderState.Error -> CameraColors.Error
        }
        if (state == ViewfinderState.Detected) {
            drawPath(frame, CameraColors.DetectedFill)
            drawPath(frame, CameraColors.Detected, style = Stroke(Dimens.DetectedHighlightStroke.toPx()))
        }

        // Rohy = obrys hledáčku oříznutý na čtyři čtverce v rozích
        val corner = Dimens.ViewfinderCornerLength.toPx()
        val stroke = Stroke(width = Dimens.ViewfinderCornerStroke.toPx(), cap = StrokeCap.Round)
        val reach = stroke.width
        listOf(
            topLeft,
            Offset(topLeft.x + side - corner, topLeft.y),
            Offset(topLeft.x, topLeft.y + side - corner),
            Offset(topLeft.x + side - corner, topLeft.y + side - corner),
        ).forEach { origin ->
            clipRect(
                left = origin.x - reach,
                top = origin.y - reach,
                right = origin.x + corner + reach,
                bottom = origin.y + corner + reach,
            ) {
                drawPath(frame, cornerColor, style = stroke)
            }
        }
    }
}

/** Horní lišta průhledná přes obraz kamery, s bílým textem a ikonami. */
@Composable
private fun CameraTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = Color.Transparent,
            onSurface = CameraColors.Content,
            onSurfaceVariant = CameraColors.Content,
        ),
    ) {
        VydajnikTopBar(title = "Skenovat kód", onBack = onBack, modifier = modifier)
    }
}

/** „Namiřte na kód“ / „Načítám kód…“ nad ovládáním. */
@Composable
private fun ScanStatusPill(reading: Boolean) {
    Surface(
        modifier = Modifier.height(Dimens.ScanPillHeight),
        shape = AppShapes.Pill,
        color = CameraColors.Pill,
        contentColor = CameraColors.Content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (reading) {
                CircularProgressIndicator(modifier = Modifier.size(Dimens.IconM), color = CameraColors.Content)
            }
            Text(
                text = if (reading) "Načítám kód…" else "Namiřte na QR platbu nebo čárový kód",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Svítilna a výběr fotky z galerie. */
@Composable
private fun CameraControls(
    torchOn: Boolean,
    hasTorch: Boolean,
    onTorchChange: (Boolean) -> Unit,
    onGallery: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxl)) {
        if (hasTorch) {
            FilledIconToggleButton(
                checked = torchOn,
                onCheckedChange = onTorchChange,
                modifier = Modifier.size(Dimens.CameraControlSize),
                shape = AppShapes.Pill,
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    containerColor = CameraColors.ControlBackground,
                    contentColor = CameraColors.Content,
                    checkedContainerColor = CameraColors.Content,
                    checkedContentColor = CameraColors.Background,
                ),
            ) {
                Icon(
                    imageVector = if (torchOn) Icons.Outlined.FlashlightOn else Icons.Outlined.FlashlightOff,
                    contentDescription = if (torchOn) "Vypnout svítilnu" else "Zapnout svítilnu",
                )
            }
        }
        FilledIconButton(
            onClick = onGallery,
            modifier = Modifier.size(Dimens.CameraControlSize),
            shape = AppShapes.Pill,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = CameraColors.ControlBackground,
                contentColor = CameraColors.Content,
            ),
        ) {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Vybrat fotku kódu z galerie")
        }
    }
}

/** Karta s chybou skenu (nečitelný kód, není QR Platba, chybí kamera) a s akcemi. */
@Composable
private fun ScanErrorCard(
    title: String,
    message: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.m),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow, contentColor = scheme.onSurface),
    ) {
        Column(Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                Box(
                    modifier = Modifier
                        .size(Dimens.Badge)
                        .background(scheme.errorContainer, AppShapes.IconBox),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = scheme.onErrorContainer,
                        modifier = Modifier.size(Dimens.BadgeIcon),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.End),
            ) {
                if (secondaryText != null && onSecondary != null) {
                    TextButton(onClick = onSecondary) { Text(secondaryText) }
                }
                Button(onClick = onPrimary) { Text(primaryText) }
            }
        }
    }
}

/* ---------------------------------------------------------------- Pomocné */

/** Obraz kamery je vždy tmavý → světlé ikony stavového řádku, po odchodu se vrátí původní. */
@Composable
private fun LightSystemBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insets = WindowCompat.getInsetsController(window, view)
        val lightStatus = insets.isAppearanceLightStatusBars
        val lightNavigation = insets.isAppearanceLightNavigationBars
        insets.isAppearanceLightStatusBars = false
        insets.isAppearanceLightNavigationBars = false
        onDispose {
            insets.isAppearanceLightStatusBars = lightStatus
            insets.isAppearanceLightNavigationBars = lightNavigation
        }
    }
}

private fun android.content.Context.isCameraPermissionGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun android.content.Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
