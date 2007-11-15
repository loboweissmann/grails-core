/**
 * @author Graeme Rocher
 * @since 1.0
 * 
 * Created: Oct 10, 2007
 */
package org.codehaus.groovy.grails.web.mapping

import org.codehaus.groovy.grails.web.servlet.mvc.AbstractGrailsControllerTests
import org.springframework.core.io.ByteArrayResource

class DoubleWildcardUrlMappingTests extends AbstractGrailsControllerTests {

    def mappingScript = '''
mappings {
    "/components/image/$path**?" {
        controller = "components"
        action = "image"
    }
    "/stuff/image/$path**" {
        controller = "components"
        action = "image"
    }    
}
    '''

    void testDoubleWildCardMatching() {

             def res = new ByteArrayResource(mappingScript.bytes)

             def evaluator = new DefaultUrlMappingEvaluator()
             def mappings = evaluator.evaluateMappings(res)


             def m = mappings[0]
             def m2 = mappings[1]
             assert m

             def info = m.match("/components/image")
             //assert !mappings[1].match("/stuff/image")

             info.configure(webRequest)

             assertEquals "components", info.controllerName
             assertEquals "image", info.actionName             
             assertNull webRequest.params.path

             info = m.match("/components/image/")
             info.configure(webRequest)

             assertEquals "components", info.controllerName
             assertEquals "image", info.actionName
             assertEquals '', webRequest.params.path             

             info = m.match("/components/image/foo.bar")
             assert info
             info.configure(webRequest)
             
             assertEquals "components", info.controllerName
             assertEquals "image", info.actionName
             assertEquals 'foo.bar', webRequest.params.path

             info = m.match('/components/image/asdf/foo.bar')
             assert info
             info.configure(webRequest)

             assertEquals "components", info.controllerName
             assertEquals "image", info.actionName
             assertEquals 'asdf/foo.bar', webRequest.params.path             

             assert !m2.match("/stuff/image")
             info = m2.match("/stuff/image/foo.bar")
             assert info
             info.configure(webRequest)

             assertEquals "components", info.controllerName
             assertEquals "image", info.actionName
             assertEquals 'foo.bar', webRequest.params.path

    }
}