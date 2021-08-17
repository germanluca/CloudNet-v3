/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.driver.network.rpc.listener;

import de.dytanic.cloudnet.driver.network.INetworkChannel;
import de.dytanic.cloudnet.driver.network.buffer.DataBuf;
import de.dytanic.cloudnet.driver.network.protocol.IPacket;
import de.dytanic.cloudnet.driver.network.protocol.IPacketListener;
import de.dytanic.cloudnet.driver.network.protocol.Packet;
import de.dytanic.cloudnet.driver.network.rpc.RPCHandler;
import de.dytanic.cloudnet.driver.network.rpc.RPCHandlerRegistry;

public class RPCPacketListener implements IPacketListener {

  private final RPCHandlerRegistry rpcHandlerRegistry;

  public RPCPacketListener(RPCHandlerRegistry rpcHandlerRegistry) {
    this.rpcHandlerRegistry = rpcHandlerRegistry;
  }

  @Override
  public void handle(INetworkChannel channel, IPacket packet) throws Exception {
    RPCHandler handler = this.rpcHandlerRegistry.getHandler(packet.getContent().readString());
    if (handler != null) {
      DataBuf response = handler.handleRPC(channel, packet.getContent());
      // if the response is null the sender is not expecting a result
      if (response != null && packet.getUniqueId() != null) {
        channel.getQueryPacketManager().sendQueryPacket(new Packet(-1, response), packet.getUniqueId());
      }
    }
  }
}
