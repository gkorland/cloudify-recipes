import java.util.concurrent.TimeUnit
import groovy.util.ConfigSlurper
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

println "Waiting (max) 2 minutes for ${config.managementService}"
mgmt=context.waitForService(config.managementService,2,TimeUnit.MINUTES)
assert (mgmt!=null),"No management services found"

mgmtservices = mgmt.waitForInstances(1, 1, TimeUnit.MINUTES)
assert (mgmtservices != null), "Unable to find 1 instance of management services"

println "invoking deploy-grid-basic with myDataGrid"
def params = new Object[1]
params[0] = "myDataGrid"
mgmtService.invoke("deploy-grid-basic", params, 3, TimeUnit.MINUTES)