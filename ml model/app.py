# --- 1. IMPORTS ---
import uvicorn
import joblib
import pandas as pd
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
import os
from typing import Dict, Any, List

# --- 2. APPLICATION SETUP ---
app = FastAPI(
    title="RTRWH & AR Potential Assessment API",
    description="An API to assess Rooftop Rainwater Harvesting and Artificial Recharge potential based on user inputs and geospatial data.",
    version="1.0.0",
)

# --- 3. DATA MODELS (PYDANTIC) ---
# Defines the structure of the API request and response bodies for data validation.

class UserInput(BaseModel):
    """Input model for the assessment endpoint."""
    name: str = Field(..., example="Arjun Sharma", description="User's full name.")
    latitude: float = Field(..., ge=-90, le=90, example=28.6139, description="Latitude of the location.")
    longitude: float = Field(..., ge=-180, le=180, example=77.2090, description="Longitude of the location.")
    num_dwellers: int = Field(..., gt=0, example=4, description="Number of people living in the house.")
    roof_area_sqm: float = Field(..., gt=0, example=150.0, description="Rooftop catchment area in square meters.")
    open_space_sqm: float = Field(..., ge=0, example=50.0, description="Available open space for recharge structures.")
    roof_type: str = Field(..., example="concrete", description="Type of roof material (e.g., 'concrete', 'tiles').")

class RWHAnalysis(BaseModel):
    """Detailed analysis of Rainwater Harvesting potential."""
    potential_annual_runoff_liters: float
    recommended_tank_size_liters: int
    notes: str

class ARAnalysis(BaseModel):
    """Detailed analysis of Artificial Recharge potential."""
    is_feasible: bool
    recommended_structure_type: str
    structure_dimensions: Dict[str, str]
    notes: str

class CostBenefitAnalysis(BaseModel):
    """Economic analysis of the proposed system."""
    estimated_initial_investment: float
    annual_operating_maintenance_cost: float
    annual_water_savings_liters: float
    annual_monetary_savings: float
    payback_period_years: float

class AssessmentResponse(BaseModel):
    """The final, structured response sent to the user."""
    location_info: Dict[str, Any]
    feasibility_score: float
    feasibility_insights: str
    rwh_analysis: RWHAnalysis
    ar_analysis: ARAnalysis
    cost_benefit_analysis: CostBenefitAnalysis

class ChatRequest(BaseModel):
    """Input model for the chatbot."""
    query: str = Field(..., example="What is a recharge pit?")

class ChatResponse(BaseModel):
    """Response model for the chatbot."""
    answer: str
    source: str

# --- 4. MOCK DATA & DATA ACQUISITION SERVICE ---
# In a real application, this service would fetch data from external APIs and databases (IMD, CGWB, PostGIS).
# Here, we simulate that behavior with mock data.

class DataAcquisitionService:
    """Simulates fetching data for a given location."""

    def __init__(self):
        # Mock database of location-specific data
        self.geo_data = {
            "delhi": {
                "avg_annual_rainfall_mm": 714.0,
                "principal_aquifer": "Alluvium",
                "soil_type": "Loamy Sand",
                "soil_permeability": "moderate",
            },
            "mumbai": {
                "avg_annual_rainfall_mm": 2422.0,
                "principal_aquifer": "Basalt",
                "soil_type": "Clay Loam",
                "soil_permeability": "low",
            },
            "jaipur": {
                "avg_annual_rainfall_mm": 605.0,
                "principal_aquifer": "Alluvium & Hard Rock",
                "soil_type": "Sandy Loam",
                "soil_permeability": "high",
            }
        }

    def get_location_data(self, lat: float, lon: float) -> Dict[str, Any]:
        """Returns mock environmental data based on rough lat/lon."""
        # Simple logic to pick a city based on latitude
        if 28 < lat < 29:
            return self.geo_data["delhi"]
        elif 18 < lat < 20:
            return self.geo_data["mumbai"]
        elif 26 < lat < 27:
            return self.geo_data["jaipur"]
        else:
            # Return a default if no match
            return self.geo_data["delhi"]

# --- 5. MACHINE LEARNING MODEL SERVICE ---
# This service handles the groundwater level prediction model.

MODEL_PATH = "groundwater_model.joblib"

def train_and_save_dummy_model():
    """
    Creates, trains, and saves a dummy ML model if it doesn't exist.
    This simulates the offline training process.
    """
    if not os.path.exists(MODEL_PATH):
        print("Dummy model not found. Training a new one...")
        # Create dummy dataset
        data = {
            'rainfall_mm': np.random.uniform(500, 2500, 100),
            'soil_permeability_encoded': np.random.randint(0, 3, 100), # 0:low, 1:moderate, 2:high
            'aquifer_type_encoded': np.random.randint(0, 3, 100) # 0:Alluvium, 1:Basalt, 2:Hard Rock
        }
        # Target variable: groundwater depth in meters below ground level (mbgl)
        data['groundwater_depth_mbgl'] = 10 + (1500 - data['rainfall_mm']) * 0.02 + data['soil_permeability_encoded'] * -2

        df = pd.DataFrame(data)
        X = df[['rainfall_mm', 'soil_permeability_encoded', 'aquifer_type_encoded']]
        y = df['groundwater_depth_mbgl']

        # Train a simple RandomForest model
        model = RandomForestRegressor(n_estimators=10, random_state=42)
        model.fit(X, y)

        # Save the model
        joblib.dump(model, MODEL_PATH)
        print(f"Dummy model saved to {MODEL_PATH}")

class GroundwaterModelService:
    """Loads and uses the pre-trained ML model for predictions."""
    def __init__(self, model_path: str):
        try:
            self.model = joblib.load(model_path)
        except FileNotFoundError:
            raise RuntimeError(f"Model file not found at {model_path}. Please run the script once to train and save the model.")

    def predict(self, rainfall_mm: float, soil_permeability: str, aquifer_type: str) -> float:
        """Predicts groundwater depth based on input features."""
        # Encode categorical features just like in the dummy training data
        perm_map = {"low": 0, "moderate": 1, "high": 2}
        aq_map = {"Alluvium": 0, "Basalt": 1, "Alluvium & Hard Rock": 2}

        # Handle potential unknown values by mapping to a default
        perm_encoded = perm_map.get(soil_permeability, 1) # Default to moderate
        aq_encoded = aq_map.get(aquifer_type, 0) # Default to Alluvium

        features = pd.DataFrame([[rainfall_mm, perm_encoded, aq_encoded]],
                                columns=['rainfall_mm', 'soil_permeability_encoded', 'aquifer_type_encoded'])
        prediction = self.model.predict(features)
        return round(float(prediction[0]), 2)


# --- 6. CORE CALCULATION SERVICE ---
# This service contains the main business logic and algorithms for the assessment.

class CoreCalculationService:
    """Handles all the core scientific and financial calculations."""

    def calculate_rwh_potential(self, roof_area_sqm: float, avg_annual_rainfall_mm: float, roof_type: str) -> float:
        """Calculates potential annual runoff in liters."""
        runoff_coefficients = {"concrete": 0.8, "tiles": 0.9}
        C = runoff_coefficients.get(roof_type.lower(), 0.75)  # Default coefficient
        i = avg_annual_rainfall_mm / 1000  # Convert rainfall to meters
        A = roof_area_sqm
        # Formula: Runoff (cubic meters) = C * i * A
        # Convert to Liters: 1 cubic meter = 1000 Liters
        return round(C * i * A * 1000, 2)

    def recommend_tank_size(self, annual_runoff_liters: float, num_dwellers: int) -> int:
        """Recommends a practical tank size."""
        # Assume daily water demand for non-potable uses is 50 liters/person
        daily_demand = num_dwellers * 50
        # Assume storage for 15 days of demand as a baseline
        demand_based_size = daily_demand * 15
        # Recommend the smaller of 20% of annual runoff or the demand-based size
        supply_based_size = annual_runoff_liters * 0.20
        recommended_size = min(demand_based_size, supply_based_size)
        # Round to nearest 500 liters
        return int(np.ceil(recommended_size / 500.0)) * 500

    def analyze_ar_feasibility(self, open_space_sqm: float, soil_permeability: str, gw_depth_mbgl: float) -> Dict:
        """Analyzes feasibility of Artificial Recharge."""
        if gw_depth_mbgl < 5:
            return {"is_feasible": False, "notes": "Groundwater level is too shallow (< 5m), posing a risk of waterlogging."}
        if soil_permeability == 'low':
            return {"is_feasible": False, "notes": "Soil permeability is too low for effective recharge."}
        if open_space_sqm < 10:
            return {"is_feasible": False, "notes": "Insufficient open space (< 10 sqm) available for a standard recharge structure."}

        # Basic logic for structure recommendation
        if open_space_sqm > 20 and soil_permeability == 'high':
            structure = "Recharge Trench"
            dims = {"Length": "5m", "Width": "2m", "Depth": "1.5m"}
        else:
            structure = "Recharge Pit"
            dims = {"Diameter": "2m", "Depth": "3m"}

        return {
            "is_feasible": True,
            "recommended_structure_type": structure,
            "structure_dimensions": dims,
            "notes": "Location is suitable for artificial recharge."
        }


    def calculate_feasibility_score(self, rainfall: float, space: float, permeability: str, gw_depth: float) -> float:
        """Calculates a weighted feasibility score from 0 to 100."""
        # Normalize factors to a 0-1 scale
        f_rainfall = min(rainfall / 1500, 1.0) # Cap at 1500mm
        f_space = min(space / 100, 1.0) # Cap at 100 sqm
        perm_map = {"low": 0.1, "moderate": 0.6, "high": 1.0}
        f_hydrogeology = perm_map.get(permeability, 0.5)
        # Invert depth score: deeper is better (up to a point)
        f_gw_depth = max(0, 1 - (5 / max(gw_depth, 5.1)))

        # Weights (summing to 1 for positive factors)
        w1, w2, w3, w4 = 0.4, 0.2, 0.4, 0.5
        score = (w1*f_rainfall + w2*f_space + w3*f_hydrogeology) * 100
        # Penalty for shallow groundwater
        if gw_depth < 5:
            score *= (1 - w4)

        return round(max(0, min(score, 100)), 2)

    def analyze_costs_benefits(self, rwh_analysis: RWHAnalysis, ar_analysis: ARAnalysis) -> CostBenefitAnalysis:
        """Performs a simplified Life Cycle Cost (LCC) analysis."""
        # Mock costs
        tank_cost_per_liter = 8  # e.g., INR 8/liter for a plastic tank
        recharge_pit_cost = 25000 # e.g., INR 25,000
        recharge_trench_cost = 40000

        inv = rwh_analysis.recommended_tank_size_liters * tank_cost_per_liter
        if ar_analysis.is_feasible:
            inv += recharge_pit_cost if "Pit" in ar_analysis.recommended_structure_type else recharge_trench_cost

        omc = inv * 0.02 # Assume 2% of investment for annual O&M

        # Mock benefits
        municipal_water_cost_per_1000l = 30 # e.g., INR 30
        annual_water_savings = rwh_analysis.potential_annual_runoff_liters
        annual_monetary_savings = (annual_water_savings / 1000) * municipal_water_cost_per_1000l

        payback = inv / annual_monetary_savings if annual_monetary_savings > 0 else float('inf')

        return CostBenefitAnalysis(
            estimated_initial_investment=round(inv, 2),
            annual_operating_maintenance_cost=round(omc, 2),
            annual_water_savings_liters=annual_water_savings,
            annual_monetary_savings=round(annual_monetary_savings, 2),
            payback_period_years=round(payback, 1)
        )

# --- 7. KNOWLEDGE BASE (CHATBOT) SERVICE ---
# In a real app, this would use a Vector DB and LLM (RAG). Here, it's a simple lookup.

class KnowledgeService:
    """Simulates a RAG-powered chatbot."""
    def __init__(self):
        self.kb = {
            "recharge pit": ("A recharge pit is a small, excavated pit, usually filled with layers of gravel and sand, that allows rainwater runoff to collect and slowly percolate into the ground, recharging the aquifer.", "CGWB Manual, Chapter 4"),
            "runoff coefficient": ("The runoff coefficient is a dimensionless number that represents the fraction of rainfall that becomes surface runoff. It depends on the surface material; for example, a concrete roof has a higher coefficient (more runoff) than a garden.", "Standard Hydrology Textbooks"),
            "feasibility": ("Feasibility for rainwater harvesting depends on local rainfall, available space for structures, soil permeability for recharge, and the depth to the groundwater table. Very shallow groundwater can cause waterlogging if recharge is attempted.", "CGWB Guidelines")
        }

    def ask(self, query: str) -> ChatResponse:
        """Finds a relevant answer from the knowledge base."""
        query_lower = query.lower()
        for keyword, (answer, source) in self.kb.items():
            if keyword in query_lower:
                return ChatResponse(answer=answer, source=source)
        return ChatResponse(answer="I'm sorry, I don't have information on that topic. Please ask about recharge pits, runoff, or feasibility.", source="Internal")

# --- 8. API ENDPOINTS ---
# These are the entry points for the application, orchestrating the services.

# Instantiate all services
data_service = DataAcquisitionService()
# First, ensure the dummy model is trained and saved
train_and_save_dummy_model()
model_service = GroundwaterModelService(MODEL_PATH)
calc_service = CoreCalculationService()
knowledge_service = KnowledgeService()

@app.post("/assess", response_model=AssessmentResponse)
def assess_rwh_potential(user_input: UserInput):
    """
    Main endpoint to perform a full on-the-spot assessment.
    """
    try:
        # 1. Data Enrichment
        location_data = data_service.get_location_data(user_input.latitude, user_input.longitude)
        rainfall = location_data['avg_annual_rainfall_mm']
        soil = location_data['soil_type']
        permeability = location_data['soil_permeability']
        aquifer = location_data['principal_aquifer']

        # 2. ML Prediction
        gw_depth = model_service.predict(rainfall, permeability, aquifer)
        location_data['predicted_groundwater_depth_mbgl'] = gw_depth

        # 3. Core Calculations
        # RWH Analysis
        potential_runoff = calc_service.calculate_rwh_potential(
            user_input.roof_area_sqm, rainfall, user_input.roof_type
        )
        tank_size = calc_service.recommend_tank_size(potential_runoff, user_input.num_dwellers)
        rwh_analysis = RWHAnalysis(
            potential_annual_runoff_liters=potential_runoff,
            recommended_tank_size_liters=tank_size,
            notes="Based on average rainfall and standard water demand for non-potable uses."
        )

        # AR Analysis
        ar_result = calc_service.analyze_ar_feasibility(
            user_input.open_space_sqm, permeability, gw_depth
        )
        ar_analysis = ARAnalysis(**ar_result)

        # Feasibility Score
        score = calc_service.calculate_feasibility_score(
            rainfall, user_input.open_space_sqm, permeability, gw_depth
        )
        insights = f"Score is primarily driven by {'high' if rainfall > 1000 else 'moderate'} rainfall and {'good' if permeability != 'low' else 'poor'} soil permeability. The groundwater level at {gw_depth}m is {'ideal' if gw_depth > 10 else 'adequate'} for recharge."

        # Cost-Benefit Analysis
        cba = calc_service.analyze_costs_benefits(rwh_analysis, ar_analysis)

        # 4. Structure and Return Response
        return AssessmentResponse(
            location_info=location_data,
            feasibility_score=score,
            feasibility_insights=insights,
            rwh_analysis=rwh_analysis,
            ar_analysis=ar_analysis,
            cost_benefit_analysis=cba
        )

    except Exception as e:
        # Generic error handling
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")


@app.post("/chat", response_model=ChatResponse)
def chat_with_knowledge_base(request: ChatRequest):
    """
    Endpoint to interact with the knowledge base chatbot.
    """
    return knowledge_service.ask(request.query)

@app.get("/", include_in_schema=False)
def root():
    return {"message": "Welcome to the RTRWH Assessment API. Go to /docs to see the API documentation."}


# --- 9. MAIN EXECUTION BLOCK ---
if __name__ == "__main__":
    # This block allows you to run the server directly from the script
    print("Starting RTRWH Assessment API server...")
    print("Access the API documentation at http://127.0.0.1:8000/docs")
    uvicorn.run(app, host="127.0.0.1", port=8000)