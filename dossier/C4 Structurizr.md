**Contexto N1 - C4**
<img width="1247" height="539" alt="{18E48208-0D7C-48FF-8067-A36DBC755069}" src="https://github.com/user-attachments/assets/d8f305a2-b399-43ae-baad-ae708e08e9b3" />


**Contenedores N2 - C4**
<img width="1301" height="563" alt="{1BCA136C-AB73-415C-BC6C-11E5413FB7F5}" src="https://github.com/user-attachments/assets/a6c9c8e0-d911-4be7-9fdf-adee8a9f4353" />


**Componentes N3 - C4**
<img width="1290" height="367" alt="{A0CB3C96-3AB3-4E0E-B728-E7845A1EBAF3}" src="https://github.com/user-attachments/assets/cf4c5eda-27a9-490c-8dd5-aeed25d388af" />


**Código utilizado**

         workspace "NOVI" "Plataforma educativa para gestión académica, comunicación grupal y herramientas de IA" {

         !identifiers hierarchical

         model {

    estudiante = person "Estudiante" "Consulta actividades, participa en grupos y utiliza herramientas de IA"

    profesor = person "Profesor" "Administra grupos, crea actividades y genera quizzes y rúbricas"


    novi = softwareSystem "NOVI" "Plataforma educativa para gestión académica, comunicación grupal y herramientas de IA" {

        web = container "Aplicación Web" "Interfaz de usuario, gestión de grupos, gestión de actividades y comunicación grupal" "React 18 + Vite + Tailwind CSS + Nginx" {

            authc = component "Autenticación" "Registro de usuarios, inicio de sesión, cierre de sesión y control de acceso"

            api = component "Cliente API" "Comunicación HTTP con el backend" "api.js"

            group = component "Gestión de Grupos" "Creación y consulta de grupos, gestión de membresías y relación usuarios-grupos" "grupos.js"

            msg = component "Gestión de Mensajes" "Envío y consulta de mensajes y comunicación grupal en tiempo real" "mensajes.js"

            act = component "Gestión de Actividades" "Creación y consulta de actividades, información de actividades y asociación con grupos" "actividades.js"

            ia = component "Integración IA" "Comunicación con el servicio externo de inteligencia artificial" "groq.js"


            tutor = component "Asistencia Académica" "Tutor NOVI, consultas académicas y apoyo a estudiantes" {
                tags "AI"
            }

            genact = component "Generación de Actividades" "Generación de títulos, generación de descripciones y contenido basado en un tema" {
                tags "AI"
            }

            quiz = component "Generación de Quizzes" "Generación de preguntas, opción múltiple y evaluación estructurada" {
                tags "AI"
            }

            rubric = component "Generación de Rúbricas" "Criterios de evaluación, niveles de evaluación e instrumentos de evaluación" {
                tags "AI"
            }

            feedback = component "Retroalimentación" "Análisis de respuestas, generación de observaciones y seguimiento académico" {
                tags "AI"
            }

            explain = component "Explicación Simplificada" "Transformación de instrucciones y contenido a un lenguaje sencillo" {
                tags "AI"
            }

            summary = component "Resumen del Grupo" "Estado de actividades, recomendaciones pedagógicas y seguimiento del grupo" {
                tags "AI"
            }
        }


        backend = container "Backend" "API, lógica de negocio, autenticación y comunicación en tiempo real" "Java 21 + Spring Boot 3.3 + Spring Security + JWT + JPA/Hibernate + WebSocket/STOMP"


        db = container "PostgreSQL" "Persistencia de datos de usuarios, grupos, membresías, mensajes, actividades y demás información del sistema" "PostgreSQL 16" {
            tags "Database"
        }
    }


    groq = softwareSystem "Groq API" "Servicio externo de Inteligencia Artificial" {
        tags "External"
    }


    // Relaciones de contexto

    estudiante -> novi "Utiliza"

    profesor -> novi "Administra y utiliza"

    novi -> groq "Solicita servicios de IA"

    groq -> novi "Devuelve respuestas generadas"


    // Relaciones entre usuarios y aplicación web

    estudiante -> novi.web "Utiliza"

    profesor -> novi.web "Administra y utiliza"


    // Relaciones entre contenedores

    novi.web -> novi.backend "Consume la API" "HTTP/REST"

    novi.web -> novi.backend "Comunicación en tiempo real" "WebSocket / STOMP"

    novi.backend -> novi.db "Consulta y almacena datos" "JPA / JDBC"

    novi.web -> groq "Solicita servicios de IA" "HTTPS / API"


    // Relaciones entre componentes y backend

    novi.web.authc -> novi.backend "Autentica usuarios" "HTTP/REST + JWT"

    novi.web.api -> novi.backend "Realiza solicitudes" "HTTP/REST"

    novi.web.group -> novi.web.api "Utiliza"

    novi.web.msg -> novi.web.api "Utiliza"

    novi.web.act -> novi.web.api "Utiliza"

    novi.web.msg -> novi.backend "Comunicación en tiempo real" "WebSocket / STOMP"


    // Integración con IA

    novi.web.ia -> groq "Solicita servicios de IA" "HTTPS / API"

    novi.web.tutor -> novi.web.ia "Utiliza"

    novi.web.genact -> novi.web.ia "Utiliza"

    novi.web.quiz -> novi.web.ia "Utiliza"

    novi.web.rubric -> novi.web.ia "Utiliza"

    novi.web.feedback -> novi.web.ia "Utiliza"

    novi.web.explain -> novi.web.ia "Utiliza"

             novi.web.summary -> novi.web.ia "Utiliza"
         }


         views {

    systemContext novi "Diagrama1" {
        include estudiante
        include profesor
        include novi
        include groq
        autolayout lr
    }


    container novi "Diagrama2" {
        include estudiante
        include profesor
        include novi.web
        include novi.backend
        include novi.db
        include groq
        autolayout lr
    }


    component novi.web "Diagrama3" {
        include *
        autolayout tb
    }


    styles {

        element "Element" {
            color #f8289c
            stroke #f8289c
            strokeWidth 7
            shape roundedbox
        }

        element "Person" {
            shape person
        }

        element "Software System" {
            shape roundedbox
        }

        element "Container" {
            shape roundedbox
        }

        element "Component" {
            shape roundedbox
        }

        element "Database" {
            shape cylinder
        }

        element "AI" {
            color #1565C0
            stroke #64B5F6
            strokeWidth 2
        }

        element "External" {
            background #999999
            color #FFFFFF
        }

        relationship "Relationship" {
            thickness 4
        }
             }
         }


         configuration {
             scope softwaresystem
         }

         }
