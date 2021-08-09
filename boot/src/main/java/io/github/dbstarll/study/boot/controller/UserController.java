package io.github.dbstarll.study.boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.Function;
import io.github.dbstarll.study.boot.model.SummaryWithTotal;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.service.PrincipalService;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map.Entry;

@RestController
@RequestMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('MODE_ADMIN')")
class UserController {
    @Autowired
    private PrincipalService principalService;
    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    SummaryWithTotal<ObjectNode> index(final PageQuery query) {
        final Bson filter = null;
        return SummaryWithTotal.warp(principalService.count(filter),
                principalService.findWithEntity(filter, query).map(new Function<Entry<Principal, ExerciseBook>, ObjectNode>() {
                    @Override
                    public ObjectNode apply(Entry<Principal, ExerciseBook> t) {
                        final ObjectNode principal = mapper.convertValue(t.getKey(), ObjectNode.class);
                        if (t.getValue() != null) {
                            principal.set("exercise", mapper.convertValue(t.getValue(), ObjectNode.class));
                        }
                        return principal;
                    }
                })).query(query);
    }
}
