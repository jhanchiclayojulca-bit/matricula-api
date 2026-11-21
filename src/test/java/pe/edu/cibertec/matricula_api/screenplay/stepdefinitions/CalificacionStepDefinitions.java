
package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import pe.edu.cibertec.matricula_api.screenplay.questions.ElHistorial;
import pe.edu.cibertec.matricula_api.screenplay.task.CalificarMatricula;
import pe.edu.cibertec.matricula_api.screenplay.task.ConsultarHistorial;
import pe.edu.cibertec.matricula_api.screenplay.task.IniciarSesion;
import pe.edu.cibertec.matricula_api.screenplay.task.ObtenerMatricula;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

public class CalificacionStepDefinitions {

    private final String baseUrl = EnvironmentSpecificConfiguration
            .from(SystemEnvironmentVariables.currentEnvironmentVariables())
            .getProperty("restapi.baseurl");

    private Integer idMatricula;

    @And("obtiene el identificador de su matrícula pagada del curso {string}")
    public void obtieneMatriculaPagada(String curso) {
        obtenerMatricula(curso, "PAGADA");
    }

    @And("obtiene el identificador de su matrícula no pagada del curso {string}")
    public void obtieneMatriculaNoPagada(String curso) {
        obtenerMatricula(curso, "REGISTRADA");
    }

    private void obtenerMatricula(String curso, String estado) {

        Actor alumno = theActorInTheSpotlight();

        alumno.attemptsTo(
                ObtenerMatricula.delCurso(curso, estado)
        );

        idMatricula = alumno.recall("idMatricula");

        if (idMatricula == null) {
            throw new AssertionError(
                    "No se pudo recuperar el identificador de la matrícula"
            );
        }
    }

    @When("el docente {string} inicia sesión correctamente")
    public void elDocenteIniciaSesion(String codigoDocente) {

        Actor docente = theActorCalled(codigoDocente)
                .whoCan(CallAnApi.at(baseUrl));

        docente.attemptsTo(
                IniciarSesion.conCredenciales(
                        codigoDocente,
                        "Cibertec2026"
                )
        );

        if (net.serenitybdd.rest.SerenityRest.lastResponse()
                .statusCode() != 200) {
            throw new AssertionError(
                    "No se pudo iniciar sesión como docente"
            );
        }
    }

    @When("registra la nota {double} en la matrícula obtenida")
    public void registraNota(double nota) {

        Actor docente = theActorInTheSpotlight();

        if (idMatricula == null) {
            throw new AssertionError(
                    "No se ha obtenido previamente el ID de matrícula"
            );
        }

        docente.remember("idMatricula", idMatricula);

        docente.attemptsTo(
                CalificarMatricula.conNota(nota)
        );
    }

    @And("la condición de la matrícula es {string}")
    public void validaCondicionMatricula(String condicion) {

        theActorInTheSpotlight().should(
                seeThat(
                        ElHistorial.condicionDeMatricula(),
                        equalTo(condicion)
                )
        );
    }

    @When("consulta su historial académico")
    public void consultaHistorial() {

        String codigoAlumno = "i202610005";

        theActorInTheSpotlight().attemptsTo(
                ConsultarHistorial.delAlumno(codigoAlumno)
        );
    }

    @And("los créditos aprobados del historial son {int}")
    public void validaCreditosAprobados(int creditos) {

        theActorInTheSpotlight().should(
                seeThat(
                        ElHistorial.creditosAprobados(),
                        equalTo(creditos)
                )
        );
    }

    @And("el promedio ponderado del historial es {double}")
    public void validaPromedioPonderado(double promedio) {

        theActorInTheSpotlight().should(
                seeThat(
                        ElHistorial.promedioPonderado(),
                        closeTo(promedio, 0.01)
                )
        );
    }
}
