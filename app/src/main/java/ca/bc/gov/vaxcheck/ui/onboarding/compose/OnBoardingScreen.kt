package ca.bc.gov.vaxcheck.ui.onboarding.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.bc.gov.vaxcheck.R

/**
 * @author: Created by Rashmi Bambhania on 23,May,2022
 */

@Composable
fun OnBoardingScreen() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.padding(64.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_educational_mobile),
            contentDescription = ""
        )
        Spacer(modifier = Modifier.padding(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = "")
            Spacer(modifier = Modifier.padding(12.dp))
            Text(text = stringResource(id = R.string.bc_camera_permission_title))
        }
        Text(text = stringResource(id = R.string.bc_camera_permission_message))
        Button(onClick = { }) {
            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = "")
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(id = R.string.bc_btn_allow_camera_access))
        }
    }
}