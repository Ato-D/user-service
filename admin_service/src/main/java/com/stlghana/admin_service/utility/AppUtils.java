package com.stlghana.admin_service.utility;

import com.stlghana.admin_service.dto.ResponseRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@AllArgsConstructor
@Slf4j
public class AppUtils {

    public static ResponseRecord getResponseRecord(String message, HttpStatus status){
        ResponseRecord responseRecord = new ResponseRecord(message, status.value(), null, ZonedDateTime.now());
        return responseRecord;

    }

    public static ResponseRecord getResponseRecord(String message, HttpStatus status, Object data){
        ResponseRecord responseRecord = new ResponseRecord(message, status.value(), data, ZonedDateTime.now());
        return responseRecord;
    }

    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_PAGE_SORT = "createdAt";
    public static final String DEFAULT_PAGE_SORT_DIR = "desc";

    /**
     * This method is used to generate a pageable to make a paginated request
     * @param params This is a Map that has the page number, size, sortBy and sortDir for the pagination
     * @return
     */
    public static Pageable getPageRequest(Map params){
        if(!isNotNullOrEmpty(params)){
            params = new HashMap();
        }
        Sort sort = Sort.by(Sort.Direction.fromString(params.getOrDefault("sortDir", AppUtils.DEFAULT_PAGE_SORT_DIR).toString()),
                params.getOrDefault("sortBy", AppUtils.DEFAULT_PAGE_SORT).toString());

        Integer pageNo = getParamToInteger(params, AppUtils.DEFAULT_PAGE_NUMBER, "page");
        Integer pageSize = getParamToInteger(params, AppUtils.DEFAULT_PAGE_SIZE, "size");

        PageRequest page = PageRequest.of(  pageNo - 1, pageSize, sort);

        return page;
    }

    /**
     * This  method is used to fetch an integer value from a Map with its default value
     * @param params The Map object
     * @param fieldName The name of the intended field
     * @param defaultVal The default value for the field to be extracted
     * @return Integer
     */
    public static Integer getParamToInteger(Map params, Integer defaultVal, String fieldName){
        Integer value = defaultVal;
        if(params != null && fieldName != null && params.get(fieldName) != null){
            try{
                var page2 = Integer.parseInt(params.get(fieldName).toString());
                if(page2 > 0){
                    value = page2;
                }
            }catch(Exception e){
                System.out.println("Invalid " + fieldName + " number");
            }
        }
        return value;
    }

    /**
     * This method is used to check if a string is not null or empty
     * @param data String value
     * @return Boolean
     * @author PrinceAh
     * @createdAt 15th July 2023
     * @modified
     * @modifiedBy
     * @modifiedAt
     */
    public static boolean isNotNullOrEmpty(Object data) {
        try{
            if (data == null) {
                return false;
            }
            if (data instanceof Collection<?>)
                return !((Collection<?>) data).isEmpty();

            if (data instanceof LinkedHashMap<?,?>)
                return !((LinkedHashMap<?, ?>) data).isEmpty();

            if (data instanceof Map<?, ?>)
                return !((Map<?, ?>) data).isEmpty();

            if (data instanceof Optional<?>)
                return ((Optional<?>) data).isPresent();

            if (data instanceof String)
                return !data.toString().trim().equalsIgnoreCase("");

            return Objects.nonNull(data);
        } catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * This method is used to parse Date from Request to ZonedDateTime
     * There are 3 checks; ZonedDateTime, LocalDateTime and LocalDate formats.
     * @param date
     * @return
     */
    public static LocalDate parseLocalDate(String date) {
        if(date == null || date.equalsIgnoreCase("")){
            return null;
        }

        /**
         * Calculating if the Date was LocalDate
         */
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            return parsedDate;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /**
         * Calculating if the Date was ZonedDateTime
         */
        try {
            ZonedDateTime parsedZone = ZonedDateTime.parse(date);
            return parsedZone.toLocalDate();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /**
         * Calculating if the Date was LocalDateTime
         */
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(date);
            return parsedDateTime.toLocalDate();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /**
         * Calculating if the Date was Timestamp
         */
        try {
            Timestamp parsedTimestamp = Timestamp.valueOf(date);
            return parsedTimestamp.toLocalDateTime().toLocalDate();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    public static ZonedDateTime parseZoneDateTime(String date){
        if(date == null || date.equalsIgnoreCase("")){
            return null;
        }
        /**
         * Calculating if the Date was ZonedDateTime
         */
        try {
            ZonedDateTime parsedZone = ZonedDateTime.parse(date);
            return parsedZone;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /**
         * Calculating if the Date was LocalDateTime
         */
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(date);
            return parsedDateTime.atZone(ZoneId.systemDefault());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /**
         * Calculating if the Date was LocalDate
         */
        try {
            LocalDate parsedLocal = LocalDate.parse(date);
            return parsedLocal.atStartOfDay(ZoneId.systemDefault());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * Filters the payload of an object and returns a map containing only the fields with string values.
     *
     * @param object The object whose payload needs to be filtered.
     * @return A map containing the filtered payload.
     * @throws IllegalAccessException If there's an issue with accessing fields from the object.
     */
    public static <T> Map<String, Object> filterPayload(T object) throws IllegalAccessException {
        Map<String, Object> payload = new HashMap<>();
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (field.getName().equals("id")) {
                    payload.put(field.getName(), value.toString());
                } else if (value instanceof String) {
                    payload.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalAccessException("Error when accessing field from object");
            }
        }
        return payload;
    }

    /**
     * This method is used to get the boolean value of an object
     * @param obj
     * @return
     * @author Prince Ah
     * @createdAt 15th July 2023
     * @modified
     * @modifiedBy
     * @modifiedAt
     */
    public static Boolean isNotAndBoolean(Object obj){
        Boolean res;
        try{
            if(!isNotNullOrEmpty(obj)){
                return null;
            }
            res = Boolean.parseBoolean(obj.toString());
        }catch (Exception e){
            res = null;
        }
        return res;
    }

    /**
     * This method is used to convert LocalDate to Date
     * @param localDate
     * @return Date
     */
    public static Date toDate (LocalDate localDate){
        if(!isNotNullOrEmpty(localDate)){
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Formats the timeline and time string into a standardized format.
     *
     * @param createdAt The string representing the timeline and time of creation.
     * @return The formatted timeline and time string.
     */
    public static String formatDate(ZonedDateTime createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return createdAt.format(formatter);
    }

    /**
     * Formats the timeline and time string into a standardized format.
     *
     * @param date The string representing the timeline and time of creation.
     * @return The formatted timeline and time string.
     */
    public static String formatDate(LocalDate date) {
        if (isNotNullOrEmpty(date)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return date.format(formatter);
        } else {
            return null;
        }

    }

    /**
     * This method is used to remove all SQL injection characters from a given String
     * @param input
     * @return String
     */
    public static String removeSQLInjection(String input) {
        if(!isNotNullOrEmpty(input)){
            return input;
        }
        // Define a regular expression pattern to match SQL injection symbols
        String regex = "[=%*'\";]";

        // Use the Pattern class to compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Use the pattern to create a matcher for the input string
        Matcher matcher = pattern.matcher(input);

        // Replace all occurrences of SQL injection symbols with an empty string
        String sanitizedInput = matcher.replaceAll("");

        return sanitizedInput.trim();
    }


    /**
     * This is used to remove anything other than alphabets. numbers and space from a given String
     * @param input
     * @return String
     */
    public static String removeAllSymbols(String input) {
        if(!isNotNullOrEmpty(input)){
            return input;
        }
        input =  input.replaceAll("[^a-zA-Z0-9\\s]>", "");

        return input.trim();
    }

    /**
     * This method is used to split and transform T, the object in question, string separated by commas into List<UUID>
     * @param data
     * @return  List<T>
     */
    public static <T> List<T> getListFromString(String data, Function<String, T> mapper) {
        try {
            if (data == null || data.trim().isEmpty()) {
                return new ArrayList<>();
            }
            String[] values = data.split(",");
            return Arrays.stream(values)
                    .map(mapper)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }


    /**
     * This is used to fetch a value from a Map
     * @param data  Map
     * @return Object from a map
     */
    public static Object getNameFromMap(Map data) {
        if(AppUtils.isNotNullOrEmpty(data)){
            return data.getOrDefault("name", null);
        }
        return null;
    }

    /**
     * This method is used to convert a camelcase String to snakeCase
     * @param value
     * @return String of snake format
     */
    public static String transformToSnake(String value){
        String str = "";
        var charList = value.toCharArray();
        for(int i = 0; i < charList.length; i++){
            if(Character.isUpperCase(charList[i])){
                str += "_";
            }
            str += Character.toLowerCase(charList[i]);
        }
        return str;
    }

    /**
     * Extracts a list of UUIDs from the specified map based on the given key.
     *
     * @param params A map containing string parameters.
     * @param key    The key for the parameter to extract UUIDs from.
     * @return       A list of UUIDs extracted from the parameter value.
     */
    public static List<UUID> extractUUIDsFromParams(Map<String, String> params, String key) {
        List<UUID> uuidList = new ArrayList<>();
        String paramValue = params != null ? params.getOrDefault(key, "") : "";

        if (!paramValue.isEmpty()) {
            String[] uuidStrings = paramValue.split(",");
            for (String uuidStr : uuidStrings) {
                uuidList.add(UUID.fromString(uuidStr));
            }
        }

        return uuidList;
    }


    /**
     * Parses a string parameter from the specified map and returns a list of strings.
     *
     * @param params     A map containing string parameters.
     * @param paramName  The name of the parameter to parse.
     * @return           A list of strings parsed from the parameter value.
     */
    public static List<String> parseStringParams(Map<String, String> params, String paramName) {
        String paramValue = params != null ? params.getOrDefault(paramName, "") : "";
        List<String> parsedList = new ArrayList<>();

        if (!paramValue.isEmpty()) {
            parsedList = Arrays.asList(paramValue.split(","));
        }

        return parsedList;
    }

    /**
     * This method maps a page of a model to a Pagination object.
     *
     * @param page The Page object containing the generic entities.
     * @return A Pagination object containing the mapped entities, page information, and total element count.
     *         Returns null if the input Page is null.
     */
    public static <T> Pagination mapToPagination(Page<T> page) {
        if (page == null) {
            return null;
        }
        return new Pagination(page.getContent(), page.getPageable(), (int) page.getTotalElements());
    }

    /**
     * Generic method to find an entity by ID.
     * Throws ResponseStatusException if the entity is not found.
     *
     * @param repository The repository to query
     * @param id The ID of the entity to find
     * @param <T> The entity type
     * @return The found entity
     */
    public static <T> T findEntityById(JpaRepository<T, UUID> repository, UUID id, String entityName) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, entityName + " record " + id + " does not exist")
        );
    }

    /**
     * Retrieves the roles assigned to the currently authenticated user.
     * Returns an empty list if the user is not authenticated or an error occurs.
     *
     * @return A collection of GrantedAuthority objects representing the user's roles.
     */
    public static Collection<? extends GrantedAuthority> getUserRoles() {
        try {
            if (authentication().isAuthenticated()) {
                return authentication().getAuthorities();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Checks if the given roles collection contains the "ADMIN" role.
     *
     * @param roles A collection of roles assigned to a user.
     * @return True if the "ADMIN" role is present, false otherwise.
     */
    public static boolean hasAdminRole(Collection<? extends GrantedAuthority> roles) {
        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.contains("ADMIN"));
    }

    public static boolean hasGeneralManagerRole(Collection<? extends  GrantedAuthority> roles) {
        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equalsIgnoreCase("GENERAL_MANAGER"));
    }

    public static boolean hasManagerRole(Collection<? extends  GrantedAuthority> roles) {
        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equalsIgnoreCase("MANAGER"));
    }

    public static boolean hasHRRole(Collection<? extends  GrantedAuthority> roles) {
        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.contains("HR"));
    }

    public static boolean hasTeamLeadRole(Collection<? extends  GrantedAuthority> roles) {
        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.contains("TEAM LEADER"));
    }


//    public static boolean hasPermission(Collection<? extends GrantedAuthority> authorities, String permission) {
//        return authorities.stream()
//                .map(GrantedAuthority::getAuthority)
//                .anyMatch(auth -> auth.equals(permission));
//    }

    public static boolean hasPermission(Collection<? extends GrantedAuthority> authorities, String permissionToCheck) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(permission -> permission.equals(permissionToCheck));
    }

    public static boolean hasPermission(UUID userId, String permissionToCheck, Collection<? extends GrantedAuthority> authorities) {
        System.out.println("Checking if user " + userId + " has permission: " + permissionToCheck);
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(permission -> permission.equals(permissionToCheck));
    }






    /**
     * Checks if the given roles collection contains any of the specified roles.
     *
     * @param roles A collection of roles assigned to a user.
     * @param str   A list of role names to check for.
     * @return True if any of the specified roles are present, false otherwise.
     */
    public static boolean hasRole(Collection<? extends GrantedAuthority> roles, List<String> str){
        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> str.stream().anyMatch(role::contains));
    }


    /**
     * Retrieves the UUID of the currently authenticated user.
     * Returns null if the user is not authenticated or an error occurs.
     *
     * @return The UUID of the authenticated user, or null if not authenticated.
     */
    public static UUID getAuthenticatedUserId() {
        try {
            if (authentication().isAuthenticated()){
                return UUID.fromString(authentication().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    private static UUID getClaimId(String claimKey) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (isNotNullOrEmpty(authentication) &&
                    authentication.isAuthenticated() &&
                    isNotNullOrEmpty(authentication.getPrincipal())) {
                var claims = ((Jwt) authentication.getPrincipal()).getClaims();
                if (isNotNullOrEmpty(claims)) {
                    var claimId = claims.getOrDefault(claimKey, null);
                    if(isNotNullOrEmpty(claimId) && claimId instanceof String){
                        return UUID.fromString((String) claimId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves username of the currently authenticated user.
     * Returns null if the user is not authenticated or an error occurs.
     *
     * @return The UUID of the authenticated user, or null if not authenticated.
     */
    public static String getAuthenticatedUserName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(isNotNullOrEmpty(authentication) &&
                    authentication.isAuthenticated() &&
                    isNotNullOrEmpty(authentication.getPrincipal())){
                var claims = ((Jwt) authentication.getPrincipal()).getClaims();
                if (isNotNullOrEmpty(claims)) {
                    var firstName = claims.getOrDefault("name", null);
                    System.out.println(firstName);
                    if(isNotNullOrEmpty(firstName)){
                        return firstName.toString();
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Retrieves the UUID of the currently authenticated user department.
     * Returns null if the user is not authenticated or an error occurs.
     *
     * @return The UUID of the authenticated user sector, or null if not authenticated or sector head.
     */
    public static UUID getAuthenticatedUserDepartmentId() {
        return getClaimId("department");
    }

    /**
     * Retrieves the UUID of the currently authenticated user company.
     * Returns null if the user is not authenticated or an error occurs.
     *
     * @return The UUID of the authenticated user sector, or null if not authenticated or sector head.
     */
    public static UUID getAuthenticatedUserCompanyId() {
        return getClaimId("company");
    }

    /**
     * Retrieves the UUID of the currently authenticated user company.
     * Returns null if the user is not authenticated or an error occurs.
     *
     * @return The UUID of the authenticated user, or null if not authenticated.
     */
    public static UUID getAuthenticatedUserManagerId() {
        return getClaimId("manager_id");
    }

    /**
     * Retrieves the current authentication object from the SecurityContextHolder.
     *
     * @return The Authentication object representing the current user's authentication.
     */
    public static Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * This method is used to fetch the Client Role from a Jwt
     * @param principal
     * @return List of client roles
     */
    public static List<String> getClientRoles(Jwt principal, String clientName){
        if(!isNotNullOrEmpty(clientName)){
            clientName = "ceptra_op";
        }
        Object resourceAccess = principal.getClaims().get("resource_access");
        if(AppUtils.isNotNullOrEmpty(resourceAccess)){
            Object clientMap = ((Map<String, Object>) resourceAccess).get(clientName);
            return getRolesFromObject(clientMap);
        }
        return new ArrayList<>();
    }

    /**
     * This method is used to fetch Roles from an Object by structuring it as a Map<String, Object>
     * @param data
     * @return List<String>
     * @author PrinceAh
     * @createdAt 30th July 2024
     * @modified
     * @modifiedBy
     * @modifiedAt
     */
    public static List<String> getRolesFromObject(Object data){
        List<String> res = new ArrayList<>();
        if(AppUtils.isNotNullOrEmpty(data)){
            try{
                if(data instanceof Collection<?>){
                    ((Collection) data).stream().forEach(i -> res.addAll(AppUtils.getRoles(i)));
                }else{
                    return getRoles(data);
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        return res;
    }


    public static List<String> getRoles(Object data){
        try{
            Object roleList = ((Map<String, Object>) data).get("roles");
            if(AppUtils.isNotNullOrEmpty(roleList)){
                return  (List<String>) roleList;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}

