package org.codehaus.groovy.grails.web.servlet.mvc

import org.springframework.validation.Errors

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class CommandObjectErrorsTests extends AbstractGrailsControllerTests {

    protected void onSetUp() {
        gcl.parseClass '''
class TestController {

    def index = { Form form ->
        [formErrors:form.hasErrors()]
    }

    def two = { Form form ->
        [formErrors:form.errors]
    }

    def three = { Form form ->
        [form:form]
    }

    def four = { Form form ->
         form.validate()
        [formErrors:form.errors]
    }

    def five = { Form form ->
        [form: new Form()]
    }

    def six = { Form form ->
        def f = new Form(url: new URL('http://www.springsource.com/'))
        f.validate()
        [formErrors: f.errors]
    }

    def validate = { Form form ->

        [formErrors:form.validate()]
    }
}
class Form {
    String input
    URL url

    static constraints = {
        input(size:5..10, nullable:false)
    }
}
'''
    }

    void testCommandObjectsDontShareErrors() {
        def controller = ga.getControllerClass("TestController").clazz.newInstance()

        def model = controller.five()

        def error
        def form = model.form
        assertNotNull 'did not find expected form', form
        assertFalse 'form should not have had errors', form.hasErrors()
    }

    void testValidatingNewlyCreatedCommandObject() {
        def controller = ga.getControllerClass("TestController").clazz.newInstance()
        def model = controller.six()
        def errors = model.formErrors
        assertNotNull 'did not find expected errors', errors
        assertEquals 1, errors.allErrors.size()
    }

    void testClearErrors() {
        def controller = ga.getControllerClass("TestController").clazz.newInstance()

        controller.params.url = "not_a_url"
        controller.params.input = "helloworld"

        def model = controller.three()

        def form = model.form
        assertNotNull 'did not find expected form', form
        assertTrue 'form should have had errors', form.hasErrors()
        form.clearErrors()
        assertFalse 'clearErrors did not work', form.hasErrors()
    }

    void testHasErrors() {
        def controller = ga.getControllerClass("TestController").clazz.newInstance()

        assertTrue controller.index()['formErrors']
    }

    void testValidate() {
        def controller = ga.getControllerClass("TestController").clazz.newInstance()

        assertFalse controller.validate()['formErrors']
    }

    void testHasBindingErrors() {

        def controller = ga.getControllerClass("TestController").clazz.newInstance()

        controller.params.url = "not_a_url"
        controller.params.input = "helloworld"

        def model = controller.two()

        Errors errors = model?.formErrors
        assert errors
        assert errors.hasErrors()
        assertEquals 1, errors.allErrors.size()
    }

    void testValidatingTwice() {
        // GRAILS-4918
        def controller = ga.getControllerClass("TestController").clazz.newInstance()

        controller.params.url = "http://grails.org"
        controller.params.input = "someverylongstringthatfailsvalidation"

        def model = controller.four()

        Errors errors = model?.formErrors
        assert errors
        assert errors.hasErrors()
        assertEquals 1, errors.allErrors.size()
    }
}
