/*
 *
 *  *
 *  *  * Copyright 2019-2020 the original author or authors.
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package org.springdoc.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.converters.AdditionalModelsConverter;
import org.springdoc.core.converters.FileSupportConverter;
import org.springdoc.core.converters.ModelConverterRegistrar;
import org.springdoc.core.converters.PolymorphicModelConverter;
import org.springdoc.core.converters.PropertyCustomizingConverter;
import org.springdoc.core.converters.ResponseSupportConverter;
import org.springdoc.core.converters.SchemaPropertyDeprecatingConverter;
import org.springdoc.core.customizers.OpenApiBuilderCustomiser;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.PropertyCustomizer;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import static org.springdoc.core.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.Constants.SPRINGDOC_SCHEMA_RESOLVE_PROPERTIES;
import static org.springdoc.core.SpringDocUtils.getConfig;

/**
 * The type Spring doc configuration.
 * @author bnasslahsen
 */
@Configuration
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
public class SpringDocConfiguration {

	/**
	 * The constant BINDRESULT_CLASS.
	 */
	private static final String BINDRESULT_CLASS = "org.springframework.boot.context.properties.bind.BindResult";

	static {
		getConfig().replaceWithSchema(ObjectNode.class, new ObjectSchema());
	}

	/**
	 * Local spring doc parameter name discoverer local variable table parameter name discoverer.
	 *
	 * @return the local variable table parameter name discoverer
	 */
	@Bean
	LocalVariableTableParameterNameDiscoverer localSpringDocParameterNameDiscoverer() {
		return new LocalVariableTableParameterNameDiscoverer();
	}

	/**
	 * Additional models converter additional models converter.
	 *
	 * @return the additional models converter
	 */
	@Bean
	@Lazy(false)
	AdditionalModelsConverter additionalModelsConverter() {
		return new AdditionalModelsConverter();
	}

	/**
	 * Property customizing converter property customizing converter.
	 *
	 * @param customizers the customizers
	 * @return the property customizing converter
	 */
	@Bean
	@Lazy(false)
	PropertyCustomizingConverter propertyCustomizingConverter(Optional<List<PropertyCustomizer>> customizers) {
		return new PropertyCustomizingConverter(customizers);
	}

	/**
	 * File support converter file support converter.
	 *
	 * @return the file support converter
	 */
	@Bean
	@ConditionalOnMissingBean
	@Lazy(false)
	FileSupportConverter fileSupportConverter() {
		return new FileSupportConverter();
	}

	/**
	 * Response support converter response support converter.
	 *
	 * @return the response support converter
	 */
	@Bean
	@ConditionalOnMissingBean
	@Lazy(false)
	ResponseSupportConverter responseSupportConverter() {
		return new ResponseSupportConverter();
	}

	/**
	 * Schema property deprecating converter schema property deprecating converter.
	 *
	 * @return the schema property deprecating converter
	 */
	@Bean
	@ConditionalOnMissingBean
	@Lazy(false)
	SchemaPropertyDeprecatingConverter schemaPropertyDeprecatingConverter() {
		return new SchemaPropertyDeprecatingConverter();
	}

	/**
	 * Polymorphic model converter polymorphic model converter.
	 *
	 * @return the polymorphic model converter
	 */
	@Bean
	@ConditionalOnMissingBean
	@Lazy(false)
	PolymorphicModelConverter polymorphicModelConverter() {
		return new PolymorphicModelConverter();
	}

	/**
	 * Open api builder open api builder.
	 *
	 * @param openAPI the open api
	 * @param context the context
	 * @param securityParser the security parser
	 * @param springDocConfigProperties the spring doc config properties
	 * @param openApiBuilderCustomisers the open api builder customisers
	 * @return the open api builder
	 */
	@Bean
	@ConditionalOnMissingBean
	OpenAPIBuilder openAPIBuilder(Optional<OpenAPI> openAPI, ApplicationContext context,
			SecurityParser securityParser,
			SpringDocConfigProperties springDocConfigProperties,
			Optional<List<OpenApiBuilderCustomiser>> openApiBuilderCustomisers) {
		return new OpenAPIBuilder(openAPI, context, securityParser, springDocConfigProperties, openApiBuilderCustomisers);
	}

	/**
	 * Model converter registrar model converter registrar.
	 *
	 * @param modelConverters the model converters
	 * @return the model converter registrar
	 */
	@Bean
	@Lazy(false)
	ModelConverterRegistrar modelConverterRegistrar(Optional<List<ModelConverter>> modelConverters) {
		return new ModelConverterRegistrar(modelConverters.orElse(Collections.emptyList()));
	}

	/**
	 * Operation builder operation builder.
	 *
	 * @param parameterBuilder the parameter builder
	 * @param requestBodyBuilder the request body builder
	 * @param securityParser the security parser
	 * @param propertyResolverUtils the property resolver utils
	 * @return the operation builder
	 */
	@Bean
	@ConditionalOnWebApplication
	@ConditionalOnMissingBean
	OperationBuilder operationBuilder(GenericParameterBuilder parameterBuilder, RequestBodyBuilder requestBodyBuilder,
			SecurityParser securityParser, PropertyResolverUtils propertyResolverUtils) {
		return new OperationBuilder(parameterBuilder, requestBodyBuilder,
				securityParser, propertyResolverUtils);
	}

	/**
	 * Property resolver utils property resolver utils.
	 *
	 * @param factory the factory
	 * @return the property resolver utils
	 */
	@Bean
	PropertyResolverUtils propertyResolverUtils(ConfigurableBeanFactory factory) {
		return new PropertyResolverUtils(factory);
	}

	/**
	 * Request body builder request body builder.
	 *
	 * @param parameterBuilder the parameter builder
	 * @return the request body builder
	 */
	@Bean
	@ConditionalOnWebApplication
	@ConditionalOnMissingBean
	RequestBodyBuilder requestBodyBuilder(GenericParameterBuilder parameterBuilder) {
		return new RequestBodyBuilder(parameterBuilder);
	}

	/**
	 * Security parser security parser.
	 *
	 * @param propertyResolverUtils the property resolver utils
	 * @return the security parser
	 */
	@Bean
	@ConditionalOnMissingBean
	SecurityParser securityParser(PropertyResolverUtils propertyResolverUtils) {
		return new SecurityParser(propertyResolverUtils);
	}

	/**
	 * Generic return type parser return type parser.
	 *
	 * @return the return type parser
	 */
	@Bean
	@Lazy(false)
	ReturnTypeParser genericReturnTypeParser() {
		return new ReturnTypeParser() {};
	}

	/**
	 * Parameter builder generic parameter builder.
	 *
	 * @param propertyResolverUtils the property resolver utils
	 * @return the generic parameter builder
	 */
	@Bean
	@ConditionalOnMissingBean
	GenericParameterBuilder parameterBuilder(PropertyResolverUtils propertyResolverUtils) {
		return new GenericParameterBuilder(propertyResolverUtils);
	}

	/**
	 * Properties resolver for schema open api customiser.
	 *
	 * @param propertyResolverUtils the property resolver utils
	 * @param openAPIBuilder the open api builder
	 * @return the open api customiser
	 */
	@Bean
	@ConditionalOnProperty(SPRINGDOC_SCHEMA_RESOLVE_PROPERTIES)
	@Lazy(false)
	OpenApiCustomiser propertiesResolverForSchema(PropertyResolverUtils propertyResolverUtils, OpenAPIBuilder openAPIBuilder) {
		return openApi -> {
			Components components = openApi.getComponents();
			Map<String, Schema> schemas = components.getSchemas();
			schemas.values().forEach(schema -> openAPIBuilder.resolveProperties(schema, propertyResolverUtils));
		};
	}

	/**
	 * Springdoc bean factory post processor bean factory post processor.
	 *
	 * @return the bean factory post processor
	 */
	@Bean
	@Conditional(CacheOrGroupedOpenApiCondition.class)
	@ConditionalOnClass(name = BINDRESULT_CLASS)
	@Lazy(false)
	static BeanFactoryPostProcessor springdocBeanFactoryPostProcessor() {
		return new SpringdocBeanFactoryConfigurer();
	}

	/**
	 * Springdoc bean factory post processor 2 bean factory post processor.
	 *
	 * @return the bean factory post processor
	 */
// For spring-boot-1 compatibility
	@Bean
	@Conditional(CacheOrGroupedOpenApiCondition.class)
	@ConditionalOnMissingClass(value = BINDRESULT_CLASS)
	@Lazy(false)
	static BeanFactoryPostProcessor springdocBeanFactoryPostProcessor2() {
		return SpringdocBeanFactoryConfigurer::initBeanFactoryPostProcessor;
	}

}
