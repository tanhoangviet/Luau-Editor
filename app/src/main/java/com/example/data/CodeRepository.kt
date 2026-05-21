package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CodeRepository(private val dao: CodeFileDao) {

    val allFiles: Flow<List<CodeFile>> = dao.getAllFiles()

    suspend fun initializeDefaultFilesIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            val initLuau = """--!strict
-- init.luau: Interactive Roblox workspace sandbox initiation
local Players = game:GetService("Players")
local Workspace = game:GetService("Workspace")

type CharacterState = {
    player: Player,
    position: Vector3,
    health: number,
    isArrested: boolean
}

local function monitorPlayer(player: Player): CharacterState
    print("Binding state hooks for player: " .. player.Name)
    local character = player.Character or player.CharacterAdded:Wait()
    local rootPart = character:WaitForChild("HumanoidRootPart") :: BasePart
    local humanoid = character:WaitForChild("Humanoid") :: Humanoid

    local currentState: CharacterState = {
        player = player,
        position = rootPart.Position,
        health = humanoid.Health,
        isArrested = false
    }
    
    print("Successfully hooked: " .. player.Name .. " at initial vector " .. tostring(currentState.position))
    return currentState
end

-- Hook default player if present
task.spawn(function()
    for _, player in Players:GetPlayers() do
        monitorPlayer(player)
    end
end)
"""

            val playerController = """--!nonstrict
-- player_controller.luau: Vector space movement controller rules
local task = require("@sys/task")

local Controller = {}
Controller.__index = Controller

type ControllerType = {
    speed: number,
    active: boolean,
    direction: Vector3
}

function Controller.new(initialSpeed: number?): ControllerType
    local self = setmetatable({}, Controller)
    self.speed = initialSpeed or 16
    self.active = true
    self.direction = Vector3.new(0, 0, 0)
    return self
end

function Controller:Move(direction: Vector3)
    if not self.active then return end
    self.direction = direction.Unit * self.speed
    print("Moving character towards: " .. tostring(self.direction))
    
    -- Simulated movement tick
    task.wait(0.1)
    print("Tick: Character translation vector adjusted.")
end

local myController = Controller.new(24)
myController:Move(Vector3.new(1, 0, 5))
"""

            val raycastAnalytics = """--!strict
-- raycast_analytics.luau: Spatial intelligence math and ray calculations
local Workspace = game:GetService("Workspace")

type RaycastInfo = {
    hitInstance: Instance?,
    distance: number,
    normal: Vector3,
    intersection: Vector3
}

local function calculateRaycastIntersection(origin: Vector3, direction: Vector3, maxDistance: number): RaycastInfo
    print("Generating workspace raycast simulation...")
    
    local raycastParams = RaycastParams.new()
    raycastParams.FilterType = Enum.RaycastFilterType.Exclude
    raycastParams.IgnoreWater = true
    
    -- Simulated hit result calculation
    local hit = true
    local simulatedDistance = math.random() * maxDistance
    
    local info: RaycastInfo = {
        hitInstance = if hit then Instance.new("Part") else nil,
        distance = simulatedDistance,
        normal = Vector3.new(0, 1, 0),
        intersection = origin + (direction.Unit * simulatedDistance)
    }
    
    if info.hitInstance then
        print("Ray hit object " .. info.hitInstance.Name .. " at distance: " .. string.format("%.2f", info.distance))
    else
        print("Raycast did not hit any Part items.")
    end
    
    return info
end

local originVector = Vector3.new(0, 50, 0)
local targetVector = Vector3.new(0, -1, 0)
calculateRaycastIntersection(originVector, targetVector, 100)
"""

            val luauConfig = """-- config.luau: Shared table configuration module
local Config = {
    workspaceId = "REBUILT-LUAU-IDE",
    systemReady = true,
    tabSpaces = 4,
    showDebugger = true,
    clientSettings = {
        themeId = "elegant_dark",
        fontSize = 14,
        allowTypeChecking = true
    }
}

return Config
"""

            // Insert initial files in Luau
            dao.insertFile(CodeFile(name = "init.luau", content = initLuau, language = "luau", isDefault = true))
            dao.insertFile(CodeFile(name = "player_controller.luau", content = playerController, language = "luau", isDefault = true))
            dao.insertFile(CodeFile(name = "raycast_analytics.luau", content = raycastAnalytics, language = "luau", isDefault = true))
            dao.insertFile(CodeFile(name = "config.luau", content = luauConfig, language = "luau", isDefault = true))
        }
    }

    suspend fun getFileById(id: Int): CodeFile? = withContext(Dispatchers.IO) {
        dao.getFileById(id)
    }

    suspend fun insertFile(file: CodeFile): Long = withContext(Dispatchers.IO) {
        dao.insertFile(file)
    }

    suspend fun updateFile(file: CodeFile) = withContext(Dispatchers.IO) {
        dao.updateFile(file)
    }

    suspend fun deleteFileById(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteFileById(id)
    }
}
