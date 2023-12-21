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

fun Node.asExternal(): com.voximplant.core.Node = when (this) {
    Node1 -> com.voximplant.core.Node.Node1
    Node2 -> com.voximplant.core.Node.Node2
    Node3 -> com.voximplant.core.Node.Node3
    Node4 -> com.voximplant.core.Node.Node4
    Node5 -> com.voximplant.core.Node.Node5
    Node6 -> com.voximplant.core.Node.Node6
    Node7 -> com.voximplant.core.Node.Node7
    Node8 -> com.voximplant.core.Node.Node8
    Node9 -> com.voximplant.core.Node.Node9
    Node10 -> com.voximplant.core.Node.Node10
}

fun com.voximplant.core.Node.asInternal(): Node = when (this) {
    com.voximplant.core.Node.Node1 -> Node1
    com.voximplant.core.Node.Node2 -> Node2
    com.voximplant.core.Node.Node3 -> Node3
    com.voximplant.core.Node.Node4 -> Node4
    com.voximplant.core.Node.Node5 -> Node5
    com.voximplant.core.Node.Node6 -> Node6
    com.voximplant.core.Node.Node7 -> Node7
    com.voximplant.core.Node.Node8 -> Node8
    com.voximplant.core.Node.Node9 -> Node9
    com.voximplant.core.Node.Node10 -> Node10
}
