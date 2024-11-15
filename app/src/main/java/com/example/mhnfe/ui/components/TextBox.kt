package com.example.mhnfe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mhnfe.R
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainRed

@Composable
fun MainTextBox(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    isError: Boolean = false,
    onIsErrorChange: (Boolean) -> Unit = {},
    inputText: String = "",
    onInputTextChange: (String) -> Unit = {},
    hintText: String = "", // setting hint text
    warningText: String = "" // setting warning text
) {
    var isFocused by remember { mutableStateOf(false) } // focus state
    val backgroundColor = Color.White

    // borderColor changed by conditions
    val borderColor = when {
        isError -> mainRed
        inputText.isEmpty() -> mainGray
        else -> mainBlack
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start
    ) {
        BasicTextField(
            modifier = modifier
                .height(39.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused }
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = if (isFocused) mainBlack else borderColor,
                    shape = RoundedCornerShape(4.dp)
                ),
            value = inputText,
            onValueChange = { newText ->
                onInputTextChange(newText)
            },
            cursorBrush = SolidColor(Color.Black),
            singleLine = true,
            // focus out when click enter
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            // innerTextField settings
            decorationBox = { innerTextField ->
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp , alignment = Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Place holder
                    Box(modifier = modifier
                        .weight(1f)
                    ) {
                        if (inputText.isEmpty() && !isFocused) {
                            Text(
                                text = hintText,
                                color = mainGray,
                                textAlign = TextAlign.Center,
                                style = Typography.bodyMedium,
                                lineHeight = 24.sp // 16.sp * 1.5
                            )
                        }
                        innerTextField()
                    }
                    // cancel button
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { onInputTextChange("") }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.cancel_button),
                                contentDescription = "Clear text",
                                modifier = modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            }
        )
        // Print warning text under text box
        if (isError) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp , alignment = Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = modifier.size(16.dp),
                    painter = painterResource(id = R.drawable.cautionicon),
                    contentDescription = null,
                    tint = mainRed
                )
                Text(
                    text = "warningText",
                    color = mainRed,
                    style = Typography.bodySmall.copy(fontSize = 12.sp)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainTextBoxPreview() {
    // FocusManager를 로컬로 가져옵니다.
    val focusManager = LocalFocusManager.current
    var text by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainTextBox(
            modifier = Modifier,
            focusManager = focusManager,
            isError = isError,
            onIsErrorChange = { isError = it },
            inputText = text,
            onInputTextChange = { newText ->
                text = newText
                // 예시로 오류 조건을 text 길이로 설정
                isError = newText.length > 3
            },
            hintText = "Enter text here...",
            warningText = "Text must be at least 3 characters"
        )
    }
}