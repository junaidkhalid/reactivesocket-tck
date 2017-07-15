/*
 * Copyright 2016 Facebook, Inc.
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 */

package io.reactivesocket.tck

import java.io.PrintWriter

import io.reactivesocket.Payload
import org.reactivestreams.{Subscriber, Subscription}

import org.json4s._
import org.json4s.native.Serialization

object SubscriberIDGen {
  var count = 0

  def currentCount(): Int = {
    count += 1
    count
  }
}

class DSLTestSubscriber(writer : PrintWriter, initData: String, initMeta: String, kind: String,
  client: DSLTestClient = null) extends Subscriber[Payload] with Subscription {

  implicit val formats = Serialization.formats(NoTypeHints)

  private var id: Int = SubscriberIDGen.currentCount()

  private var clientID: Int = ClientIDGen.currentCount()
  if (client != null) clientID = client.getID

  // decide what type of subscriber to write down
  if (kind.equals("")) writer.write("") // write nothing
  else writer.write("c" + clientID + "%%" + "subscribe%%" + kind + "%%" + "s" + this.getID + "%%" + initData + "%%" +
    initMeta + "\n")

  def getID: String = return "s" + this.id.toString

  override def onSubscribe(s: Subscription) : Unit =  {}

  override def onNext(t: Payload) : Unit = {}

  override def onError(t: Throwable) : Unit = {}

  override def onComplete() : Unit = {}

  override def request(n: Long) : Unit =
    writer.write("c" + clientID + "%%" + "request%%" + n + "%%" + "s" + this.id + "\n")

  override def cancel() : Unit = writer.write("c" + clientID + "%%" + "cancel%%" + "s" + this.id + "\n")


  // assertion tests

  def assertNoErrors() : Unit = writer.write("c" + clientID + "%%" + "assert%%no_error%%" + "s" + this.id + "\n")

  def assertError() : Unit = writer.write("c" + clientID + "%%" + "assert%%error%%" + "s" + this.id + "\n")

  def assertReceived(lst: List[(String, String)]) : Unit =
    writer.write("c" + clientID + "%%" + "assert%%received%%" + "s" + this.id + "%%" + printList(lst) + "\n")

  def assertReceivedCount(n: Long) : Unit =
    writer.write("c" + clientID + "%%" + "assert%%received_n%%" + "s" + this.id + "%%" + n + "\n")

  def assertReceivedAtLeast(n: Long) : Unit =
    writer.write("c" + clientID + "%%" + "assert%%received_at_least%%" + "s" + this.id + "%%" + n + "\n")

  def assertCompleted() : Unit = writer.write("c" + clientID + "%%" + "assert%%completed%%" + "s" + this.id + "\n")

  def assertNotCompleted() : Unit =
    writer.write("c" + clientID + "%%" + "assert%%no_completed%%" + "s" + this.id + "\n")

  def assertCanceled() : Unit = writer.write("c" + clientID + "%%" + "assert%%canceled%%" + "s" + this.id + "\n")

  // await

  def awaitTerminal() : Unit = writer.write("c" + clientID + "%%" + "await%%terminal%%" + "s" + this.id + "\n")

  def awaitAtLeast(n: Long) =
    writer.write("c" + clientID + "%%" + "await%%atLeast%%" + "s" + this.id + "%%" + n + "%%" + 100 + "\n")

  def awaitNoAdditionalEvents(t: Long) =
    writer.write("c" + clientID + "%%" + "await%%no_events%%" + "s" + this.id + "%%" + t + "\n")

  def take(n: Long) : Unit = writer.write("c" + clientID + "%%" + "take%%" + n + "%%" + "s" + this.id + "\n")

  // internal functions

  private def printList(lst: List[(String, String)]) : String = lst.map(a => a._1 + "," + a._2).mkString("&&")
}