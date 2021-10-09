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

package de.dytanic.cloudnet.command.sub;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.command.source.CommandSource;
import de.dytanic.cloudnet.common.unsafe.CPUUsageResolver;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;
import java.util.List;

public class CommandMe {

  @CommandDescription("Displays all important information about this process and the JVM")
  @CommandMethod("me|info")
  public void me(CommandSource commandSource) {
    CloudNet cloudNet = CloudNet.getInstance();
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    List<String> messages = Arrays.asList(
      " ",
      "CloudNet " + de.dytanic.cloudnet.deleted.command.commands.CommandMe.class.getPackage().getImplementationTitle()
        + " " + de.dytanic.cloudnet.deleted.command.commands.CommandMe.class.getPackage().getImplementationVersion()
        + " by Dytanic & the CloudNet Community",
      "Discord: https://discord.cloudnetservice.eu/",
      " ",
      "ClusterId: " + cloudNet.getConfig().getClusterConfig().getClusterId(),
      "NodeId: " + cloudNet.getConfig().getIdentity().getUniqueId(),
      "Head-NodeId: " + cloudNet.getClusterNodeServerProvider().getHeadNode().getNodeInfo().getUniqueId(),
      "CPU usage: (P/S) " + CPUUsageResolver.CPU_USAGE_OUTPUT_FORMAT.format(CPUUsageResolver.getProcessCPUUsage()) + "/"
        +
        CPUUsageResolver.CPU_USAGE_OUTPUT_FORMAT.format(CPUUsageResolver.getSystemCPUUsage()) + "/100%",
      "Node services memory allocation (U/R/M): " + cloudNet.getCurrentNetworkClusterNodeInfoSnapshot()
        .getUsedMemory() + "/" +
        cloudNet.getCurrentNetworkClusterNodeInfoSnapshot().getReservedMemory() + "/" +
        cloudNet.getCurrentNetworkClusterNodeInfoSnapshot().getMaxMemory() + " MB",
      "Threads: " + Thread.getAllStackTraces().keySet().size(),
      "Heap usage: " + (memoryMXBean.getHeapMemoryUsage().getUsed() / 1048576) + "/" + (
        memoryMXBean.getHeapMemoryUsage().getMax() / 1048576) + "MB",
      " "
    );
    commandSource.sendMessage(messages);
  }
}
