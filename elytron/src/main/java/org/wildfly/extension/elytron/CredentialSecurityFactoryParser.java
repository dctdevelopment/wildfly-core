/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
package org.wildfly.extension.elytron;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.isNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.CREDENTIAL_SECURITY_FACTORIES;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.CUSTOM_CREDENTIAL_SECURITY_FACTORY;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.DEBUG;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.KERBEROS_SECURITY_FACTORY;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.MECHANISM_NAMES;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.MECHANISM_OIDS;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.MINIMUM_REMAINING_LIFETIME;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.NAME;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.OBTAIN_KERBEROS_TICKET;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.PATH;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.PRINCIPAL;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.RELATIVE_TO;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.REQUEST_LIFETIME;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.REQUIRED;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.SERVER;
import static org.wildfly.extension.elytron.ElytronDescriptionConstants.WRAP_GSS_CREDENTIAL;
import static org.wildfly.extension.elytron.ElytronSubsystemParser.readCustomComponent;
import static org.wildfly.extension.elytron.ElytronSubsystemParser.writeCustomComponent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;
import org.wildfly.security.SecurityFactory;

/**
 * Parser and Marshaller for {@link SecurityFactory<org.wildfly.security.credential.Credential>} resources.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class CredentialSecurityFactoryParser {

    private final ElytronSubsystemParser elytronSubsystemParser;

    CredentialSecurityFactoryParser(ElytronSubsystemParser elytronSubsystemParser) {
        this.elytronSubsystemParser = elytronSubsystemParser;
    }

    private void verifyNamespace(XMLExtendedStreamReader reader) throws XMLStreamException {
        elytronSubsystemParser.verifyNamespace(reader);
    }

    void readCredentialSecurityFactories(PathAddress parentAddress, XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {
        requireNoAttributes(reader);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            verifyNamespace(reader);
            String localName = reader.getLocalName();
            switch (localName) {
                case CUSTOM_CREDENTIAL_SECURITY_FACTORY:
                    readCustomComponent(CUSTOM_CREDENTIAL_SECURITY_FACTORY, parentAddress, reader, operations);
                    break;
                case KERBEROS_SECURITY_FACTORY:
                    readKerberosSecurityFactory(parentAddress, reader, operations);
                    break;
                default:
                    throw unexpectedElement(reader);
            }
        }
    }

    private void readKerberosSecurityFactory(PathAddress parentAddress, XMLExtendedStreamReader reader, List<ModelNode> operations)
            throws XMLStreamException {
        ModelNode add = new ModelNode();
        add.get(OP).set(ADD);

        Set<String> requiredAttributes = new HashSet<String>(Arrays.asList(new String[] { NAME, PRINCIPAL, PATH }));
        String name = null;

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String value = reader.getAttributeValue(i);
            if (!isNoNamespaceAttribute(reader, i)) {
                throw unexpectedAttribute(reader, i);
            } else {
                String attribute = reader.getAttributeLocalName(i);
                requiredAttributes.remove(attribute);
                switch (attribute) {
                    case NAME:
                        name = value;
                        break;
                    case PRINCIPAL:
                        KerberosSecurityFactoryDefinition.PRINCIPAL.parseAndSetParameter(value, add, reader);
                        break;
                    case PATH:
                        KerberosSecurityFactoryDefinition.PATH.parseAndSetParameter(value, add, reader);
                        break;
                    case RELATIVE_TO:
                        FileAttributeDefinitions.RELATIVE_TO.parseAndSetParameter(value, add, reader);
                        break;
                    case SERVER:
                        KerberosSecurityFactoryDefinition.SERVER.parseAndSetParameter(value, add, reader);
                        break;
                    case OBTAIN_KERBEROS_TICKET:
                        KerberosSecurityFactoryDefinition.OBTAIN_KERBEROS_TICKET.parseAndSetParameter(value, add, reader);
                        break;
                    case MINIMUM_REMAINING_LIFETIME:
                        KerberosSecurityFactoryDefinition.MINIMUM_REMAINING_LIFETIME.parseAndSetParameter(value, add, reader);
                        break;
                    case REQUEST_LIFETIME:
                        KerberosSecurityFactoryDefinition.REQUEST_LIFETIME.parseAndSetParameter(value, add, reader);
                        break;
                    case DEBUG:
                        KerberosSecurityFactoryDefinition.DEBUG.parseAndSetParameter(value, add, reader);
                        break;
                    case WRAP_GSS_CREDENTIAL:
                        KerberosSecurityFactoryDefinition.WRAP_GSS_CREDENTIAL.parseAndSetParameter(value, add, reader);
                        break;
                    case REQUIRED:
                        KerberosSecurityFactoryDefinition.REQUIRED.parseAndSetParameter(value, add, reader);
                        break;
                    case MECHANISM_NAMES:
                        for (String mechanismName : reader.getListAttributeValue(i)) {
                            KerberosSecurityFactoryDefinition.MECHANISM_NAMES.parseAndAddParameterElement(mechanismName, add, reader);
                        }
                        break;
                    case MECHANISM_OIDS:
                        for (String mechanismOid : reader.getListAttributeValue(i)) {
                            KerberosSecurityFactoryDefinition.MECHANISM_OIDS.parseAndAddParameterElement(mechanismOid, add, reader);
                        }
                        break;
                    default:
                        throw unexpectedAttribute(reader, i);
                }
            }
        }

        if (requiredAttributes.isEmpty() == false) {
            throw missingRequired(reader, requiredAttributes);
        }
        if (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            KerberosSecurityFactoryDefinition.OPTIONS.getParser().parseElement(KerberosSecurityFactoryDefinition.OPTIONS, reader, add);
        }

        add.get(OP_ADDR).set(parentAddress.toModelNode()).add(KERBEROS_SECURITY_FACTORY, name);

        operations.add(add);
    }

    private void startCredentialSecurityFactories(boolean started, XMLExtendedStreamWriter writer) throws XMLStreamException {
        if (started == false) {
            writer.writeStartElement(CREDENTIAL_SECURITY_FACTORIES);
        }
    }

    private boolean writeCustomCredenitalSecurityFactories(boolean started, ModelNode subsystem, XMLExtendedStreamWriter writer) throws XMLStreamException {
        if (subsystem.hasDefined(CUSTOM_CREDENTIAL_SECURITY_FACTORY)) {
            startCredentialSecurityFactories(started, writer);
            ModelNode securityFactories = subsystem.require(CUSTOM_CREDENTIAL_SECURITY_FACTORY);
            for (String name : securityFactories.keys()) {
                ModelNode factory = securityFactories.require(name);

                writeCustomComponent(CUSTOM_CREDENTIAL_SECURITY_FACTORY, name, factory, writer);
            }

            return true;
        }

        return false;
    }

    private boolean writeKerberosSecurityFactories(boolean started, ModelNode subsystem, XMLExtendedStreamWriter writer) throws XMLStreamException {
        if (subsystem.hasDefined(KERBEROS_SECURITY_FACTORY)) {
            startCredentialSecurityFactories(started, writer);

            ModelNode securityFactories = subsystem.require(KERBEROS_SECURITY_FACTORY);
            for (String name : securityFactories.keys()) {
                writer.writeStartElement(KERBEROS_SECURITY_FACTORY);
                writer.writeAttribute(NAME, name);
                ModelNode factory = securityFactories.require(name);
                KerberosSecurityFactoryDefinition.PRINCIPAL.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.PATH.marshallAsAttribute(factory, false, writer);
                FileAttributeDefinitions.RELATIVE_TO.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.MINIMUM_REMAINING_LIFETIME.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.REQUEST_LIFETIME.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.SERVER.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.OBTAIN_KERBEROS_TICKET.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.DEBUG.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.WRAP_GSS_CREDENTIAL.marshallAsAttribute(factory, false, writer);

                KerberosSecurityFactoryDefinition.REQUIRED.marshallAsAttribute(factory, false, writer);
                KerberosSecurityFactoryDefinition.MECHANISM_NAMES.getMarshaller().marshallAsAttribute(KerberosSecurityFactoryDefinition.MECHANISM_NAMES, factory, false, writer);
                KerberosSecurityFactoryDefinition.MECHANISM_OIDS.getMarshaller().marshallAsAttribute(KerberosSecurityFactoryDefinition.MECHANISM_OIDS, factory, false, writer);

                KerberosSecurityFactoryDefinition.OPTIONS.marshallAsElement(factory, writer);
                writer.writeEndElement();
            }
            return true;
        }
        return false;
    }

    void writeCredentialSecurityFactories(ModelNode subsystem, XMLExtendedStreamWriter writer) throws XMLStreamException {
        boolean credentialSecurityFactoriesStarted = false;

        credentialSecurityFactoriesStarted = credentialSecurityFactoriesStarted | writeCustomCredenitalSecurityFactories(credentialSecurityFactoriesStarted, subsystem, writer);
        credentialSecurityFactoriesStarted = credentialSecurityFactoriesStarted | writeKerberosSecurityFactories(credentialSecurityFactoriesStarted, subsystem, writer);

        if (credentialSecurityFactoriesStarted) {
            writer.writeEndElement();
        }
    }


}
