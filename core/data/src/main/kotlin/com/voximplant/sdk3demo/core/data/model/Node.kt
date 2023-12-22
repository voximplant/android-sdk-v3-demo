package com.voximplant.sdk3demo.core.data.model

import com.voximplant.sdk3demo.core.model.data.Node
import com.voximplant.sdk3demo.core.model.data.Node1
import com.voximplant.sdk3demo.core.model.data.Node10
import com.voximplant.sdk3demo.core.model.data.Node2
import com.voximplant.sdk3demo.core.model.data.Node3
import com.voximplant.sdk3demo.core.model.data.Node4
import com.voximplant.sdk3demo.core.model.data.Node5
import com.voximplant.sdk3demo.core.model.data.Node6
import com.voximplant.sdk3demo.core.model.data.Node7
import com.voximplant.sdk3demo.core.model.data.Node8
import com.voximplant.sdk3demo.core.model.data.Node9

fun Node.asExternal(): com.voximplant.android.sdk.core.Node = when (this) {
    Node1 -> com.voximplant.android.sdk.core.Node.Node1
    Node2 -> com.voximplant.android.sdk.core.Node.Node2
    Node3 -> com.voximplant.android.sdk.core.Node.Node3
    Node4 -> com.voximplant.android.sdk.core.Node.Node4
    Node5 -> com.voximplant.android.sdk.core.Node.Node5
    Node6 -> com.voximplant.android.sdk.core.Node.Node6
    Node7 -> com.voximplant.android.sdk.core.Node.Node7
    Node8 -> com.voximplant.android.sdk.core.Node.Node8
    Node9 -> com.voximplant.android.sdk.core.Node.Node9
    Node10 -> com.voximplant.android.sdk.core.Node.Node10
}
