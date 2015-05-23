package org.project.neutrino.nfvo.core.utils;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope("prototype")
public class NSDUtils {

    @Autowired
    @Qualifier("vimRepository")
    private GenericRepository<VimInstance> vimRepository;

    public void fetchData(NetworkServiceDescriptor networkServiceDescriptor) throws NoResultException{

        /**
         * Fetch datacenters..
         *
         */
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
                String id = vdu.getVimInstance().getId();
                if (id != null) {
                    VimInstance vimInstance = vimRepository.find(id);
                    vdu.setVimInstance(vimInstance);
                }
            }
        }
    }
}