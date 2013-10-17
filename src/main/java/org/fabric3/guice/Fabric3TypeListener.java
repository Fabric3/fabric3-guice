/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.guice;

import java.lang.reflect.Field;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.fabric3.api.annotation.Producer;
import org.fabric3.api.node.Domain;
import org.oasisopen.sca.annotation.Reference;

/**
 * Handles injection and endpoint publication for Guice injection types.
 */
public class Fabric3TypeListener implements TypeListener {
    private Domain domain;

    public Fabric3TypeListener(Domain domain) {
        this.domain = domain;
    }

    public <T> void hear(TypeLiteral<T> literal, TypeEncounter<T> encounter) {

        for (Field field : literal.getRawType().getDeclaredFields()) {
            if (field.isAnnotationPresent(Reference.class)) {
                // inject reference annotations
                Object proxy = domain.getService(field.getType());
                Fabric3FieldInjector<T> injector = new Fabric3FieldInjector<T>(field, proxy);
                encounter.register(injector);
            } else if (field.isAnnotationPresent(Producer.class)) {
                // inject producer annotations
                Producer producer = field.getAnnotation(Producer.class);
                String channelName = producer.value().length() > 1 ? producer.value() : field.getName();
                Object proxy = domain.getChannel(field.getType(), channelName);
                Fabric3FieldInjector<T> injector = new Fabric3FieldInjector<T>(field, proxy);
                encounter.register(injector);
            }
        }

    }

}
