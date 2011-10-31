/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.graphics.kube

/** 
 * 
 * A 4x4 float matrix
 *
 */
class M4 {
  import M4._

  val m = new Array[Float](N*N)

  def this(other: M4) {
    this()
    for (i <- 0 until m.length by N; j <- 0 until N)
      m(i+j) = other.m(i+j)
  }

  def multiply(src: GLVertex, dest: GLVertex) {
    dest.x = src.x * m(0) + src.y * m(N  ) + src.z * m(2*N  ) + m(3*N)
    dest.y = src.x * m(1) + src.y * m(N+1) + src.z * m(2*N+1) + m(3*N+1)
    dest.z = src.x * m(2) + src.y * m(N+2) + src.z * m(2*N+2) + m(3*N+2)
  }

  def multiply(other: M4): M4 = {
    val result = new M4()
    val m1 = m
    val m2 = other.m

    for (i <- 0 until m.length by N; j <- 0 until N)
      result.m(i+j) = m1(i  )*m2(    j) + m1(i+1)*m2(  N+j) +
                      m1(i+2)*m2(2*N+j) + m1(i+3)*m2(3*N+j)
    result
  }
  
  def setIdentity() {
    for (i <- 0 until m.length by N; j <- 0 until N)
      m(i+j) = if (i == j) 1f else 0f
  }
  
  override def toString: String = {
    val builder = new StringBuilder("[ ")
    for (i <- 0 until m.length by N; j <- 0 until N) {
      builder append m(i+j)
      builder append " "
      if (i < 2)
        builder append "\n  "
    }
    builder append " ]"
    builder.toString
  }
}

object M4 {
  final val N = 4
}

