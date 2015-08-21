/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.Set;

/**
 * Created by lto on 30/06/15.
 */
@Service
@Scope
public class DependencyManagement implements org.project.openbaton.nfvo.core.interfaces.DependencyManagement {

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;

    @Autowired
    @Qualifier("vnfmManager")
    private VnfmManager vnfmManager;

    @Autowired
    private org.project.openbaton.nfvo.core.interfaces.DependencyQueuer dependencyQueuer;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public int provisionDependencies(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NoResultException, NotFoundException, InterruptedException {
        NetworkServiceRecord nsr = nsrRepository.find(virtualNetworkFunctionRecord.getParent_ns_id());
        log.debug("Found NSR");
        if (nsr.getStatus().ordinal() != Status.ERROR.ordinal()) {
            Set<VNFRecordDependency> vnfRecordDependencies = nsr.getVnf_dependency();
            log.debug("Found VNF; there are " + vnfRecordDependencies.size() + " dependencies");
            for (VNFRecordDependency vnfRecordDependency : vnfRecordDependencies){
                log.trace(vnfRecordDependency.getTarget().getId() + " == " + virtualNetworkFunctionRecord.getId());
                if (vnfRecordDependency.getTarget().getId().equals(virtualNetworkFunctionRecord.getId())){
                    int dep = vnfRecordDependency.getParameters().keySet().size();
                    log.debug("Found " + dep + " for VNFR " + virtualNetworkFunctionRecord.getName() + " ( " + virtualNetworkFunctionRecord.getId() + " ) ");
                    //waiting for them to finish
                    dependencyQueuer.waitForVNFR(vnfRecordDependency.getId(), vnfRecordDependency.getNameType().keySet());
                    return dep;
                }
            }
            log.debug("Found 0 for VNFR " + virtualNetworkFunctionRecord.getName() + " ( " + virtualNetworkFunctionRecord.getId() + " ) ");
            return 0;
        }
        else return -1;
    }
}
