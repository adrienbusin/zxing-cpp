/*
* Copyright 2021 Axel Waggershauser
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.zxingcppdemo

import android.graphics.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.decathlon.vitamin.compose.chips.VitaminChips
import com.decathlon.vitamin.compose.foundation.VitaminTheme
import com.google.zxing.*
import com.zxingcpp.BarcodeReader

data class ScanOptions(
    val readerOptions: BarcodeReader.Options,
    val pauseEnabled: Boolean = false,
    val torchEnabled: Boolean = false,
    val cropEnabled: Boolean = true,
    val doSaveImage: Boolean = false
)

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val overlayView = remember {
                PreviewOverlay(this@ComposeActivity, null)
            }

            val scanOptions = remember {
                mutableStateOf(ScanOptions(BarcodeReader.Options()))
            }

            val results = remember { mutableStateListOf<String>() }

            val controller = remember {

                LifecycleCameraController(this@ComposeActivity).apply {
                    // Only enable Image Analysis
                    setEnabledUseCases(CameraController.IMAGE_ANALYSIS)

                    val size = CameraController.OutputSize(AspectRatio.RATIO_16_9)

                    previewTargetSize = size
                    imageAnalysisTargetSize = size
                }.apply {
                    // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                    // thus we can just runs the analyzer itself on main thread.
                    setImageAnalysisAnalyzer(
                        /* executor = */
                        ContextCompat.getMainExecutor(this@ComposeActivity),
                        /* analyzer = */
                        ZXingBarcodeAnalyzer(
                            scanResult = { result: String, pointFS: List<PointF>?, imageProxy: ImageProxy ->
                                results.add(result)
                            },
                            scanOptions = scanOptions,
                            previewOverlay = overlayView,
                        )
                    )

                    enableTorch(scanOptions.value.torchEnabled)
                }
            }

            VitaminTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(modifier = Modifier.fillMaxSize(), controller = controller)
                    Bottom(list = results)
                    if (scanOptions.value.cropEnabled) {
                        ViewFinder(controller)
                    }
                    Options(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = if (results.isNotEmpty()) 170.dp else 50.dp),
                        scanOptions = scanOptions,
                    )
                }
            }
        }
    }

    @Composable
    private fun ViewFinder(controller: LifecycleCameraController) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Add focus on tap
                    detectTapGestures {
                        val meteringPointFactory = SurfaceOrientedMeteringPointFactory(
                            size.width.toFloat(),
                            size.height.toFloat()
                        )

                        val meteringAction = FocusMeteringAction
                            .Builder(
                                meteringPointFactory.createPoint(it.x, it.y),
                                FocusMeteringAction.FLAG_AF
                            )
                            .disableAutoCancel()
                            .build()

                        controller.cameraControl?.startFocusAndMetering(meteringAction)
                    }
                }) {
            val borderColor = VitaminTheme.colors.vtmnBorderPrimary
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                drawRect(Color.Black.copy(alpha = 0.5f))

                val rectSize = size.minDimension / 3 * 2

                drawRoundRect(
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear,
                    size = Size(
                        width = rectSize,
                        height = rectSize
                    ),
                    topLeft = Offset(
                        (size.width - rectSize) / 2f,
                        (size.height - rectSize) / 2f
                    ),
                    cornerRadius = CornerRadius(x = 10.dp.toPx(), y = 10.dp.toPx()),
                )

                drawRoundRect(
                    color = borderColor,
                    size = Size(
                        width = rectSize,
                        height = rectSize
                    ),
                    topLeft = Offset(
                        (size.width - rectSize) / 2f,
                        (size.height - rectSize) / 2f
                    ),
                    cornerRadius = CornerRadius(x = 10.dp.toPx(), y = 10.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }

    @Composable
    fun Options(
        modifier: Modifier,
        scanOptions: MutableState<ScanOptions>,
    ) {

        val qrCodeEnable = remember { mutableStateOf(false) }
        val tryHarderEnable = remember { mutableStateOf(false) }
        val tryRotateEnable = remember { mutableStateOf(false) }
        val tryInvertEnable = remember { mutableStateOf(false) }
        val tryDownscaleEnable = remember { mutableStateOf(false) }

        LazyRow(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            item {
                VitaminChips.Filter(
                    label = "XML",
                    onClick = {
                        finish()
                    },
                    selected = false,
                )
            }

            item {
                VitaminChips.Filter(
                    label = "pause",
                    onClick = {
                        val scanEnabled = scanOptions.value.pauseEnabled
                        scanOptions.value = scanOptions.value.copy(pauseEnabled = !scanEnabled)
                    },
                    selected = scanOptions.value.pauseEnabled,
                )
            }

            item {
                VitaminChips.Filter(
                    label = "QRCode",
                    onClick = {
                        qrCodeEnable.value = !qrCodeEnable.value
                        val readerOptions = scanOptions.value.readerOptions
                        scanOptions.value = scanOptions.value.copy(
                            readerOptions = readerOptions.copy(
                                formats = if (qrCodeEnable.value) setOf(
                                    BarcodeReader.Format.QR_CODE
                                ) else setOf()
                            )
                        )
                    },
                    selected = qrCodeEnable.value,
                )
            }
            item {
                VitaminChips.Filter(
                    label = "tryHarder",
                    onClick = {
                        tryHarderEnable.value = !tryHarderEnable.value
                        val readerOptions = scanOptions.value.readerOptions
                        scanOptions.value = scanOptions.value.copy(
                            readerOptions = readerOptions.copy(tryHarder = tryHarderEnable.value)
                        )
                    },
                    selected = tryHarderEnable.value,
                )
            }
            item {
                VitaminChips.Filter(
                    label = "tryRotate",
                    onClick = {
                        tryRotateEnable.value = !tryRotateEnable.value
                        val readerOptions = scanOptions.value.readerOptions
                        scanOptions.value = scanOptions.value.copy(
                            readerOptions = readerOptions.copy(tryRotate = tryRotateEnable.value)
                        )
                    },
                    selected = tryRotateEnable.value,
                )
            }
            item {
                VitaminChips.Filter(
                    label = "tryInvert",
                    onClick = {
                        tryInvertEnable.value = !tryInvertEnable.value
                        val readerOptions = scanOptions.value.readerOptions
                        scanOptions.value = scanOptions.value.copy(
                            readerOptions = readerOptions.copy(tryInvert = tryInvertEnable.value)
                        )
                    },
                    selected = tryInvertEnable.value,
                )
            }
            item {
                VitaminChips.Filter(
                    label = "tryDownscale",
                    onClick = {
                        tryDownscaleEnable.value = !tryDownscaleEnable.value
                        val readerOptions = scanOptions.value.readerOptions
                        scanOptions.value = scanOptions.value.copy(
                            readerOptions = readerOptions.copy(tryDownscale = tryDownscaleEnable.value)
                        )
                    },
                    selected = tryDownscaleEnable.value,
                )
            }
            item {
                VitaminChips.Filter(
                    label = "crop",
                    onClick = {
                        val cropEnabled = scanOptions.value.cropEnabled
                        scanOptions.value = scanOptions.value.copy(cropEnabled = !cropEnabled)
                    },
                    selected = scanOptions.value.cropEnabled,
                )
            }
            item {
                VitaminChips.Filter(
                    label = "torch",
                    onClick = {
                        val torchEnabled = scanOptions.value.torchEnabled
                        scanOptions.value = scanOptions.value.copy(torchEnabled = !torchEnabled)
                    },
                    selected = scanOptions.value.torchEnabled,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Bottom(list: List<String>) {
        if (list.isNotEmpty()) {
            BottomSheetScaffold(
                sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                sheetPeekHeight = 150.dp,
                sheetContent = {
                    val distinct =
                        list.reversed()
                            .groupingBy { it }
                            .eachCount()
                            .filter { it.value > 0 }
                            .toList()

                    LazyColumn(
                        Modifier
                            .padding(16.dp)
                    ) {
                        items(distinct) { item ->
                            val data = list.first { it == item.first }
                            Text(
                                text = data,
                                style = VitaminTheme.typography.h6
                            )
                            Text(
                                text = "QTY ${item.second}",
                                style = VitaminTheme.typography.subtitle1
                            )
                        }
                    }
                },
                content = {}
            )
        }
    }

    @Composable
    private fun CameraPreview(
        controller: LifecycleCameraController,
        modifier: Modifier = Modifier,
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current

        AndroidView(factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        }, modifier = modifier)
    }
}
