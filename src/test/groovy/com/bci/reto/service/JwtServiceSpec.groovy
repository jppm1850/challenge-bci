package com.bci.reto.service

import com.bci.entity.User
import com.bci.exception.InvalidTokenException
import com.bci.service.impl.JwtServiceImpl
import io.jsonwebtoken.JwtException
import spock.lang.Specification

class JwtServiceSpec extends Specification {
    JwtServiceImpl jwtService

    def setup() {
        jwtService = new JwtServiceImpl(
                "4qhq8LrEBfYcaRHxhdb9zURb2rf8e7Ud8GLO9L6brain2rvUKu7C",
                86400000L // 24 horas
        )
    }

    def "should generate valid JWT token"() {
        given:
        def user = new User()
        user.setId(UUID.randomUUID())
        user.setEmail("test@example.com")

        when:
        def token = jwtService.generateToken(user)

        then:
        token != null
        token.split("\\.").length == 3
    }

    def "should generate token without userId when user id is null"() {
        given:
        def user = new User()
        user.setEmail("test@example.com")

        when:
        def token = jwtService.generateToken(user)

        then:
        token != null
        token.split("\\.").length == 3
    }

    def "should validate token and return email"() {
        given:
        def user = new User()
        user.setId(UUID.randomUUID())
        user.setEmail("test@example.com")
        def token = jwtService.generateToken(user)

        when:
        def email = jwtService.validateTokenAndGetEmail(token)

        then:
        email == user.getEmail()
    }

    def "should throw InvalidTokenException for invalid token"() {
        given:
        def invalidToken = "invalid.token.here"

        when:
        jwtService.validateTokenAndGetEmail(invalidToken)

        then:
        thrown(InvalidTokenException)
    }

    def "should throw InvalidTokenException for malformed token"() {
        given:
        def malformedToken = "not-a-jwt"

        when:
        jwtService.validateTokenAndGetEmail(malformedToken)

        then:
        thrown(InvalidTokenException)
    }

    def "should throw InvalidTokenException for empty token"() {
        given:
        def emptyToken = ""

        when:
        jwtService.validateTokenAndGetEmail(emptyToken)

        then:
        thrown(InvalidTokenException)
    }
}