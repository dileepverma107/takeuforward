import logging
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import GPT2LMHeadModel, GPT2Tokenizer
import torch

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

# Load GPT-2 model and tokenizer
model_name = "gpt2"
try:
    tokenizer = GPT2Tokenizer.from_pretrained(model_name)
    model = GPT2LMHeadModel.from_pretrained(model_name)
    logger.info(f"Loaded {model_name} model and tokenizer successfully")
except Exception as e:
    logger.error(f"Error loading model: {str(e)}")
    raise

# Define a Pydantic model for the request body
class GenerateRequest(BaseModel):
    prompt: str
    max_length: int = 200
    temperature: float = 0.7
    top_k: int = 50
    top_p: float = 0.95

@app.post("/generate/")
async def generate(request: GenerateRequest):
    try:
        inputs = tokenizer.encode(request.prompt, return_tensors="pt")
        
        # Check if CUDA is available and move model and inputs to GPU if so
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        model.to(device)
        inputs = inputs.to(device)
        
        outputs = model.generate(
            inputs, 
            max_length=request.max_length, 
            num_return_sequences=1,
            temperature=request.temperature,
            top_k=request.top_k,
            top_p=request.top_p,
            no_repeat_ngram_size=2
        )
        
        text = tokenizer.decode(outputs[0], skip_special_tokens=True)
        logger.info(f"Generated text for prompt: {request.prompt[:30]}...")
        return {"generated_text": text}
    except Exception as e:
        logger.error(f"Error generating text: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/")
async def root():
    return {"message": "Welcome to the GPT-2 text generation API"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)