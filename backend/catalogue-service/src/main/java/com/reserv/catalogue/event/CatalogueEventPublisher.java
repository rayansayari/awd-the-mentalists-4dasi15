package com.reserv.catalogue.event;

import com.reserv.catalogue.entity.Service;

/**
 * Contrat de publication des evenements metier du Service Catalogue vers l'exchange RabbitMQ {@code awd.events}.
 */
public interface CatalogueEventPublisher {

    void publishServiceCreated(Service service);

    void publishServiceUpdated(Service service);

    void publishServiceDesactivated(Service service);

    void publishServiceDeleted(Long serviceId);
}
