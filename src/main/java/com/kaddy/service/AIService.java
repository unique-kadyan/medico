package com.kaddy.service;

import com.kaddy.dto.ai.*;
import com.kaddy.repository.MedicationRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class AIService {

    private final OpenAiService openAiService;
    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    public AIService(@Value("${openai.api-key:}") String apiKey, MedicationRepository medicationRepository) {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        } else {
            this.openAiService = null;
            log.warn("OpenAI API key not configured. AI features will use fallback responses.");
        }
    }

    public SymptomAnalysisResponse analyzeSymptoms(SymptomAnalysisRequest request) {
        log.info("Analyzing symptoms: {}", request.getSymptoms());

        if (openAiService == null) {
            return getFallbackSymptomAnalysis(request);
        }

        try {
            String prompt = buildSymptomAnalysisPrompt(request);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder().model(model)
                    .messages(Arrays.asList(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                            "You are a medical AI assistant helping to analyze symptoms. "
                                    + "Provide possible conditions, urgency level, and recommended actions. "
                                    + "Always recommend consulting a healthcare professional for proper diagnosis. "
                                    + "Respond in JSON format."),
                            new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                    .maxTokens(1000).temperature(0.3).build();

            String response = openAiService.createChatCompletion(completionRequest).getChoices().get(0).getMessage()
                    .getContent();

            return parseSymptomAnalysisResponse(response, request);
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            return getFallbackSymptomAnalysis(request);
        }
    }

    public DrugInteractionResponse checkDrugInteractions(DrugInteractionRequest request) {
        log.info("Checking drug interactions for: {}", request.getMedications());

        List<String> medications = request.getMedications();
        DrugInteractionResponse response = new DrugInteractionResponse();
        response.setMedications(medications);
        response.setInteractions(new ArrayList<>());

        if (medications.size() < 2) {
            response.setHasInteractions(false);
            response.setSummary("At least two medications are required to check for interactions.");
            return response;
        }

        if (openAiService == null) {
            return getFallbackDrugInteractionCheck(request);
        }

        try {
            String prompt = buildDrugInteractionPrompt(medications);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder().model(model)
                    .messages(Arrays.asList(
                            new ChatMessage(ChatMessageRole.SYSTEM.value(),
                                    "You are a pharmacology expert AI. Analyze potential drug interactions. "
                                            + "Provide severity levels (NONE, MILD, MODERATE, SEVERE, CONTRAINDICATED) "
                                            + "and detailed explanations. Respond in JSON format."),
                            new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                    .maxTokens(1500).temperature(0.2).build();

            String aiResponse = openAiService.createChatCompletion(completionRequest).getChoices().get(0).getMessage()
                    .getContent();

            return parseDrugInteractionResponse(aiResponse, medications);
        } catch (Exception e) {
            log.error("Error checking drug interactions", e);
            return getFallbackDrugInteractionCheck(request);
        }
    }

    public SmartSchedulingResponse suggestAppointmentSlots(SmartSchedulingRequest request) {
        log.info("Generating smart scheduling suggestions for: {}", request.getPatientCondition());

        SmartSchedulingResponse response = new SmartSchedulingResponse();
        response.setSuggestedSlots(new ArrayList<>());

        if (openAiService == null) {
            return getFallbackSchedulingSuggestion(request);
        }

        try {
            String prompt = buildSchedulingPrompt(request);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder().model(model)
                    .messages(Arrays.asList(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                            "You are a medical scheduling AI. Suggest optimal appointment times based on "
                                    + "patient condition urgency, doctor specialization needs, and best practices. "
                                    + "Respond in JSON format."),
                            new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                    .maxTokens(800).temperature(0.4).build();

            String aiResponse = openAiService.createChatCompletion(completionRequest).getChoices().get(0).getMessage()
                    .getContent();

            return parseSchedulingResponse(aiResponse, request);
        } catch (Exception e) {
            log.error("Error generating scheduling suggestions", e);
            return getFallbackSchedulingSuggestion(request);
        }
    }

    public MedicalSummaryResponse generateMedicalSummary(MedicalSummaryRequest request) {
        log.info("Generating medical summary for patient");

        if (openAiService == null) {
            return getFallbackMedicalSummary(request);
        }

        try {
            String prompt = buildMedicalSummaryPrompt(request);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder().model(model)
                    .messages(Arrays.asList(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                            "You are a medical AI assistant. Generate a concise, professional medical summary "
                                    + "based on the patient information provided. Include key findings, recommendations, "
                                    + "and follow-up suggestions. Respond in JSON format."),
                            new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                    .maxTokens(1200).temperature(0.3).build();

            String aiResponse = openAiService.createChatCompletion(completionRequest).getChoices().get(0).getMessage()
                    .getContent();

            return parseMedicalSummaryResponse(aiResponse);
        } catch (Exception e) {
            log.error("Error generating medical summary", e);
            return getFallbackMedicalSummary(request);
        }
    }

    private String buildSymptomAnalysisPrompt(SymptomAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following symptoms and provide possible conditions:\n\n");
        prompt.append("Symptoms: ").append(String.join(", ", request.getSymptoms())).append("\n");

        if (request.getDuration() != null) {
            prompt.append("Duration: ").append(request.getDuration()).append("\n");
        }
        if (request.getSeverity() != null) {
            prompt.append("Severity: ").append(request.getSeverity()).append("\n");
        }
        if (request.getPatientAge() != null) {
            prompt.append("Patient Age: ").append(request.getPatientAge()).append("\n");
        }
        if (request.getPatientGender() != null) {
            prompt.append("Patient Gender: ").append(request.getPatientGender()).append("\n");
        }
        if (request.getMedicalHistory() != null && !request.getMedicalHistory().isEmpty()) {
            prompt.append("Medical History: ").append(String.join(", ", request.getMedicalHistory())).append("\n");
        }

        prompt.append("\nProvide response in this JSON format:\n");
        prompt.append("{\n");
        prompt.append(
                "  \"possibleConditions\": [{\"name\": \"...\", \"probability\": \"HIGH/MEDIUM/LOW\", \"description\": \"...\"}],\n");
        prompt.append("  \"urgencyLevel\": \"EMERGENCY/URGENT/SOON/ROUTINE\",\n");
        prompt.append("  \"recommendedSpecialist\": \"...\",\n");
        prompt.append("  \"immediateActions\": [\"...\"],\n");
        prompt.append("  \"warningSignsToWatch\": [\"...\"]\n");
        prompt.append("}");

        return prompt.toString();
    }

    private String buildDrugInteractionPrompt(List<String> medications) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Check for drug interactions between these medications:\n");
        prompt.append(String.join(", ", medications));
        prompt.append("\n\nProvide response in this JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"interactions\": [{\n");
        prompt.append("    \"drug1\": \"...\",\n");
        prompt.append("    \"drug2\": \"...\",\n");
        prompt.append("    \"severity\": \"NONE/MILD/MODERATE/SEVERE/CONTRAINDICATED\",\n");
        prompt.append("    \"description\": \"...\",\n");
        prompt.append("    \"recommendation\": \"...\"\n");
        prompt.append("  }],\n");
        prompt.append("  \"overallRisk\": \"LOW/MEDIUM/HIGH\",\n");
        prompt.append("  \"generalRecommendations\": [\"...\"]\n");
        prompt.append("}");

        return prompt.toString();
    }

    private String buildSchedulingPrompt(SmartSchedulingRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Suggest optimal appointment scheduling for:\n");
        prompt.append("Condition: ").append(request.getPatientCondition()).append("\n");

        if (request.getUrgencyLevel() != null) {
            prompt.append("Urgency: ").append(request.getUrgencyLevel()).append("\n");
        }
        if (request.getPreferredTimeOfDay() != null) {
            prompt.append("Preferred Time: ").append(request.getPreferredTimeOfDay()).append("\n");
        }

        prompt.append("\nProvide response in this JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendedSpecialist\": \"...\",\n");
        prompt.append("  \"suggestedTimeframe\": \"...\",\n");
        prompt.append("  \"appointmentDuration\": 30,\n");
        prompt.append("  \"preparationInstructions\": [\"...\"],\n");
        prompt.append("  \"priority\": \"HIGH/MEDIUM/LOW\"\n");
        prompt.append("}");

        return prompt.toString();
    }

    private String buildMedicalSummaryPrompt(MedicalSummaryRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a medical summary based on:\n");
        prompt.append("Diagnosis: ").append(request.getDiagnosis()).append("\n");

        if (request.getSymptoms() != null) {
            prompt.append("Symptoms: ").append(String.join(", ", request.getSymptoms())).append("\n");
        }
        if (request.getTreatmentProvided() != null) {
            prompt.append("Treatment: ").append(request.getTreatmentProvided()).append("\n");
        }
        if (request.getMedicationsPrescribed() != null) {
            prompt.append("Medications: ").append(String.join(", ", request.getMedicationsPrescribed())).append("\n");
        }

        prompt.append("\nProvide response in this JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"...\",\n");
        prompt.append("  \"keyFindings\": [\"...\"],\n");
        prompt.append("  \"treatmentPlan\": \"...\",\n");
        prompt.append("  \"followUpRecommendations\": [\"...\"],\n");
        prompt.append("  \"patientInstructions\": [\"...\"]\n");
        prompt.append("}");

        return prompt.toString();
    }

    private SymptomAnalysisResponse parseSymptomAnalysisResponse(String response, SymptomAnalysisRequest request) {
        SymptomAnalysisResponse result = new SymptomAnalysisResponse();
        result.setSymptoms(request.getSymptoms());
        result.setAiResponse(response);
        result.setDisclaimer("This is an AI-generated analysis and should not replace professional medical advice. "
                + "Please consult a healthcare provider for proper diagnosis and treatment.");
        return result;
    }

    private DrugInteractionResponse parseDrugInteractionResponse(String response, List<String> medications) {
        DrugInteractionResponse result = new DrugInteractionResponse();
        result.setMedications(medications);
        result.setAiResponse(response);
        result.setHasInteractions(
                response.toLowerCase().contains("severe") || response.toLowerCase().contains("moderate"));
        result.setDisclaimer(
                "This drug interaction check is AI-generated and may not include all possible interactions. "
                        + "Always consult with a pharmacist or physician before starting new medications.");
        return result;
    }

    private SmartSchedulingResponse parseSchedulingResponse(String response, SmartSchedulingRequest request) {
        SmartSchedulingResponse result = new SmartSchedulingResponse();
        result.setPatientCondition(request.getPatientCondition());
        result.setAiResponse(response);
        return result;
    }

    private MedicalSummaryResponse parseMedicalSummaryResponse(String response) {
        MedicalSummaryResponse result = new MedicalSummaryResponse();
        result.setAiGeneratedSummary(response);
        result.setGeneratedAt(java.time.LocalDateTime.now());
        return result;
    }

    private SymptomAnalysisResponse getFallbackSymptomAnalysis(SymptomAnalysisRequest request) {
        SymptomAnalysisResponse response = new SymptomAnalysisResponse();
        response.setSymptoms(request.getSymptoms());
        response.setUrgencyLevel("CONSULT_REQUIRED");
        response.setRecommendedSpecialist("General Practitioner");
        response.setDisclaimer("AI analysis is currently unavailable. Please consult a healthcare provider "
                + "for proper evaluation of your symptoms.");
        response.setPossibleConditions(Collections.emptyList());
        return response;
    }

    private DrugInteractionResponse getFallbackDrugInteractionCheck(DrugInteractionRequest request) {
        DrugInteractionResponse response = new DrugInteractionResponse();
        response.setMedications(request.getMedications());
        response.setHasInteractions(false);
        response.setSummary("AI drug interaction check is currently unavailable. "
                + "Please consult with a pharmacist to verify drug interactions.");
        response.setInteractions(Collections.emptyList());
        response.setDisclaimer("Always verify drug interactions with a qualified pharmacist or physician.");
        return response;
    }

    private SmartSchedulingResponse getFallbackSchedulingSuggestion(SmartSchedulingRequest request) {
        SmartSchedulingResponse response = new SmartSchedulingResponse();
        response.setPatientCondition(request.getPatientCondition());
        response.setRecommendedSpecialist("General Practitioner");
        response.setSuggestedTimeframe("Within 1-2 weeks based on condition");
        response.setPriority("MEDIUM");
        return response;
    }

    private MedicalSummaryResponse getFallbackMedicalSummary(MedicalSummaryRequest request) {
        MedicalSummaryResponse response = new MedicalSummaryResponse();
        response.setAiGeneratedSummary(
                "AI summary generation is currently unavailable. " + "Please review the patient records manually.");
        response.setGeneratedAt(java.time.LocalDateTime.now());
        return response;
    }
}
